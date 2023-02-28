package com.heima.article.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleLoadMode;
import com.heima.minio.service.FileStorageService;
import com.heima.model.article.dto.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    @Resource
    private ApArticleMapper apArticleMapper;
    @Resource
    private ApArticleContentMapper contentMapper;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private Configuration configuration;

    private final static short MAX_PAGE_SIZE = 50;

    /**
     * 根据参数加载文章列
     * @param dto      前端传递参数
     * @return         返回结果集
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto) {
        //1.检查参数
        Integer size = dto.getSize();
        if(size ==null|| size ==0){
            size=10;
        }
        //保存头条消息不超过范围
        size=Math.min(size,MAX_PAGE_SIZE);
        dto.setSize(size);
        //2.loaddir检查
        //2.1 loaddir为空,设置默认值1
        if(dto.getLoaddir()==null){
            dto.setLoaddir(1);
        }
        //3 tag校验
        if(StringUtils.isEmpty(dto.getTag())){
            //3.1为空,赋默认值
            dto.setTag(ArticleLoadMode.DEFAULT_TAG);
        }
        //4 时间校验
        if(dto.getMaxBehotTime()==null)dto.setMaxBehotTime(new Date(20000000000000L));
        if(dto.getMinBehotTime()==null)dto.setMinBehotTime(new Date());
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto);
        LambdaQueryWrapper<ApArticleContent> lqw = new LambdaQueryWrapper<>();
        //5.生成静态页面
        List<ApArticle> apArticleList = apArticles.stream().peek(apArticle -> {
            //5.1判断staticUrl是否为null
            if (apArticle.getStaticUrl() == null) {
                //5.2查询apArticleContent,构造条件
                lqw.eq(ApArticleContent::getArticleId, apArticle.getId());
                ApArticleContent content = contentMapper.selectOne(lqw);
                //5.3 上传静态页面到minIO
                String staticUrl = createStaticUrl(content);
                //5.4设置staticUrl
                if (staticUrl != null) {
                    apArticle.setStaticUrl(staticUrl);
                }
                apArticleMapper.updateById(apArticle);
            }
        }).collect(Collectors.toList());
        //6.返回数据
        return ResponseResult.okResult(apArticleList);
    }


    /**
     * 生成静态页面,并获取访问路径
     * @param content 文章内容
     * @return  返回页面访问路径
     * @throws Exception 抛出异常
     */
    public String createStaticUrl(ApArticleContent content){
        //1.获取文章内容
        if(content != null && StringUtils.isNotBlank(content.getContent())){
            //2.文章内容通过freemarker生成html文件
            StringWriter out = new StringWriter();
            Template template = null;
            try {
                template = configuration.getTemplate("article.ftl");
                Map<String, Object> params = new HashMap<>();
                params.put("content", JSONArray.parseArray(content.getContent()));
                template.process(params,out);
                InputStream is = new ByteArrayInputStream(out.toString().getBytes());
                //3.通过上传html文件到minio中,获取访问路径
                return fileStorageService.uploadHtmlFile("", content.getArticleId() + ".html", is);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
