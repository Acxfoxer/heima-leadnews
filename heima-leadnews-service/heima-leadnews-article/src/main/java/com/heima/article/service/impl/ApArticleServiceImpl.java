package com.heima.article.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApAuthorMapper;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleLoadMode;
import com.heima.common.constants.MqConstants;
import com.heima.common.exceptionHandle.CustomException;
import com.heima.minio.service.FileStorageService;
import com.heima.model.article.dto.ArticleDto;
import com.heima.model.article.dto.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.pojos.WmNews;
import com.heima.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 18727
 */
@Service
@Transactional(rollbackFor = CustomException.class)
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    @Resource
    private ApArticleMapper apArticleMapper;
    @Resource
    private ApArticleContentMapper contentMapper;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private Configuration configuration;
    @Resource
    private ApArticleConfigMapper apArticleConfigMapper;
    @Resource
    private ApAuthorMapper apAuthorMapper;
    @Resource
    KafkaTemplate<String,String> kafkaTemplate;
    private final static short MAX_PAGE_SIZE = 50;

    /**
     * ???????????????????????????
     * @param dto      ??????????????????
     * @return         ???????????????
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto) {
        //1.????????????
        Integer size = dto.getSize();
        if(size ==null|| size ==0){
            size=10;
        }
        //?????????????????????????????????
        size=Math.min(size,MAX_PAGE_SIZE);
        dto.setSize(size);
        //2.loaddir??????
        //2.1 loaddir??????,???????????????1
        if(dto.getLoaddir()==null){
            dto.setLoaddir(1);
        }
        //3 tag??????
        if(StringUtils.isEmpty(dto.getTag())){
            //3.1??????,????????????
            dto.setTag(ArticleLoadMode.DEFAULT_TAG);
        }
        //4 ????????????
        if(dto.getMaxBehotTime()==null) {
            //????????????,?????????????????????
            dto.setMaxBehotTime(new Date());
        }
        if(dto.getMinBehotTime()==null) {
            //??????????????????,??????????????????????????????
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
            calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH,-1);
            dto.setMinBehotTime(calendar.getTime());
        }
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto);
        LambdaQueryWrapper<ApArticleContent> lqw = new LambdaQueryWrapper<>();
        //5.??????????????????
        List<ApArticle> apArticleList = apArticles.stream().peek(apArticle -> {
            //5.1??????staticUrl?????????null
            if (apArticle.getStaticUrl() == null) {
                //5.2??????apArticleContent,????????????
                lqw.eq(ApArticleContent::getArticleId, apArticle.getId());
                ApArticleContent content = contentMapper.selectOne(lqw);
                if(content!=null){
                    //5.3 ?????????????????????minIO
                    String staticUrl = createStaticUrl(apArticle,content.getContent());
                    //5.4??????staticUrl
                    if (staticUrl != null) {
                        apArticle.setStaticUrl(staticUrl);
                    }
                }
                apArticleMapper.updateById(apArticle);
            }
        }).collect(Collectors.toList());
        //6.????????????
        return ResponseResult.okResult(apArticleList);
    }

    /**
     * ????????????
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveOrUpdate(@NotNull ArticleDto dto) {
        ApArticle apArticle = new ApArticle();
        ApArticleContent content = new ApArticleContent();
        //1.??????????????????ap_author????????????,???????????????
        LambdaQueryWrapper<ApAuthor> lq= new LambdaQueryWrapper<ApAuthor>();
        lq.eq(ApAuthor::getName, dto.getAuthorName());
        ApAuthor apAuthor = apAuthorMapper.selectOne(lq);
        ApAuthor author = new ApAuthor();
        if(apAuthor==null){
            author.setCreatedTime(new Date());
            author.setName(dto.getAuthorName());
            //????????????,?????????2
            author.setType((short) 2);
            author.setWmUserId(dto.getWmUserId());
            apAuthorMapper.insert(author);
            Short id = author.getId();
            author.setUserId(id);
            apArticle.setAuthorId(id.longValue());
            apAuthorMapper.updateById(author);
        }
        //2.???????????????????????? ap_article_content
        //??????id????????????,???id????????????
        if(dto.getId()==null){
            BeanUtils.copyProperties(dto,apArticle);
            //2.1????????????
            this.save(apArticle);
            //3.?????????????????? ap_article_content
            content.setArticleId(apArticle.getId());
            content.setContent(dto.getContent());
            contentMapper.insert(content);
            //4.????????????????????????
            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setIsComment(1);
            apArticleConfig.setIsForward(1);
            apArticleConfig.setIsDelete(0);
            apArticleConfig.setIsDown(0);
            apArticleConfig.setArticleId(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);
        }else {
            BeanUtils.copyProperties(dto,apArticle);
            if(dto.getAuthorId()==null){
                ApAuthor author1 = apAuthorMapper.selectOne(lq);
                apArticle.setAuthorId(Long.valueOf(author1.getId().toString()));
            }
            //5????????????????????????
            String staticUrl = createStaticUrl(apArticle, dto.getContent());
            apArticle.setStaticUrl(staticUrl);
            this.updateById(apArticle);
            //6??????????????????
            content.setContent(dto.getContent());
            LambdaQueryWrapper<ApArticleContent> lqw = new LambdaQueryWrapper<>();
            contentMapper.update(content, lqw.eq(ApArticleContent::getArticleId,dto.getId()));
            //7???????????????kafka
            publishToKafka(dto, apArticle);
        }
        //????????????id
        return ResponseResult.okResult(apArticle.getId());
    }

    private void publishToKafka(ArticleDto dto, ApArticle apArticle){
        ObjectMapper mapper = new ObjectMapper();
        SearchArticleVo searchArticleVo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle,searchArticleVo);
        searchArticleVo.setContent(dto.getContent());
        String str = null;
        try {
            str = mapper.writeValueAsString(searchArticleVo);
        } catch (JsonProcessingException e) {
            log.error("???????????????:{}",e.getMessage());
        }
        kafkaTemplate.send(MqConstants.AP_ARTICLE_SAVE,"searchArticleVo",str).addCallback(
                new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("???????????????:{}",ex.getMessage());
                    }
                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        log.info("????????????");
                    }
                }
        );
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @Override
    public List<SearchArticleVo> getSearchArticleVo() {
        return apArticleMapper.getResult();
    }

    /**
     * ??????????????????,?????????????????????
     * @param content ????????????
     * @return  ????????????????????????
     * @throws Exception ????????????
     */
    public String createStaticUrl(ApArticle article, String content){
        //1.??????????????????
        if(StringUtils.isNotBlank(content)){
            //2.??????????????????freemarker??????html??????
            StringWriter out = new StringWriter();
            Template template = null;
            try {
                template = configuration.getTemplate("article.ftl");
                Map<String, Object> params = new HashMap<>(520);
                ObjectMapper mapper = new ObjectMapper();
                params.put("content", mapper.readValue(content,List.class));
                template.process(params,out);
                InputStream is = new ByteArrayInputStream(out.toString().getBytes());
                //3.????????????html?????????minio???,??????????????????
                return fileStorageService.uploadHtmlFile("", article.getId() + ".html", is);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
