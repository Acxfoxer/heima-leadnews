package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.constants.MqConstants;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.constants.WmNewsConstants;
import com.heima.common.exceptionHandle.CustomException;
import com.heima.common.springutil.SpringUtil;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.feign.article.ArticleFeignClient;
import com.heima.feign.article.TaskInfoClient;
import com.heima.minio.service.FileStorageService;
import com.heima.model.article.dto.ArticleDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.media.dto.WmNewsDto;
import com.heima.model.media.dto.WmNewsPageReqDto;
import com.heima.model.media.pojos.*;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.utils.common.UserThreadLocalUtils;
import com.heima.wemedia.mapper.*;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author 18727
 */
@Service
@Transactional(rollbackFor = {CustomException.class})
@Slf4j
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Resource
    WmNewsMapper newsMapper;
    @Resource
    WmNewsMaterialMapper wmNewsMaterialMapper;
    @Resource
    WmChannelMapper wmChannelMapper;
    @Resource
    WmMaterialMapper wmMaterialMapper;
    @Resource
    FileStorageService fileStorageService;
    @Resource
    WmUserMapper wmUserMapper;
    @Resource
    ArticleFeignClient articleFeignClient;
    @Resource
    private GreenTextScan greenTextScan;
    @Resource
    private GreenImageScan greenImageScan;
    @Resource
    private WmSensitiveMapper wmSensitiveMapper;
    @Resource
    private Tess4jClient tess4jClient;
    @Resource
    private SpringUtil util;
    @Resource(name ="LeeAsyncExecutor")
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    TaskInfoClient client;
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    /**
     * ??????
     * @param dto ??????
     * @return
     */
    @Override
    public PageResponseResult toPage(WmNewsPageReqDto dto) {
        //????????????
        dto.checkParam();
        IPage<WmNews> iPage = new Page<>(dto.getPage(),dto.getSize());
        //????????????
        LambdaQueryWrapper<WmNews> lqw = new LambdaQueryWrapper<>();
        //????????????
        lqw.eq(dto.getStatus()!=null,WmNews::getStatus,dto.getStatus());
        //????????????????????????
        lqw.like(dto.getKeyword()!=null,WmNews::getTitle,dto.getKeyword());
        //??????????????????
        lqw.eq(dto.getChannelId()!=null,WmNews::getChannelId,dto.getChannelId());
        //????????????id
        lqw.eq(WmNews::getUserId,UserThreadLocalUtils.get());
        //??????????????????
        if(dto.getBeginPubDate()!=null&&dto.getEndPubDate()!=null){
            lqw.between(WmNews::getPublishTime,dto.getBeginPubDate(),dto.getEndPubDate());
        }
        this.page(iPage,lqw);
        PageResponseResult result = new PageResponseResult(dto.getPage(),dto.getSize(), (int) iPage.getTotal());
        result.setData(iPage.getRecords());
        return result;
    }
    /**
     * ???????????????,??????????????????
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitToDraft(WmNewsDto dto) {
        //0.????????????
        if(dto == null || dto.getContent() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //1.?????????????????????
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto,wmNews);
        //2.??????????????????
        if(dto.getImages()!=null&&dto.getImages().size()>0){
            String images = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(images);
        }
        //2.?????????????????????,??????type???null
        List<String> image = new ArrayList<>();
        if(dto.getType().equals((short)-1)){
            wmNews.setType(null);
            List<Map> maps = JSON.parseArray(wmNews.getContent(), Map.class);
            maps.forEach(map->{
                if("image".equals(map.get("type"))){
                    image.add(map.get("value").toString());
                }
            });
            String images = StringUtils.join(image, ",");
            wmNews.setImages(images);
        }
        //3.??????dto????????????id,??????????????????news??????,????????????
        saveOrUpdateWmNews(wmNews);
        //4.???????????????,??????????????????
        if(dto.getStatus().equals(WmNews.Status.NORMAL.getCode())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        //5.??????????????????????????????????????????
        saveRelationInfoContent(dto,image,wmNews);
        //6 ??????????????????????????????????????????
        saveRelationInfo(dto,image,wmNews);
        addTack(wmNews);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * ????????????
     * @param wmNews
     */
    public void addTack(WmNews wmNews){
        Taskinfo taskinfo = new Taskinfo();
        if(wmNews.getPublishTime()==null){
            taskinfo.setExecuteTime(new Date());
        }else {
            taskinfo.setExecuteTime(wmNews.getPublishTime());
        }
        taskinfo.setParameters(JSON.toJSONString(wmNews));
        taskinfo.setTaskType(1);
        taskinfo.setMqRoutingKey(MqConstants.DELAY_KEY);
        taskinfo.setVersion(1);
        taskinfo.setStatus(ScheduleConstants.SCHEDULED);
        taskinfo.setMqExchange(MqConstants.DELAY_EXCHANGE);
        taskinfo.setPriority(1);
        client.addTask(taskinfo);
    }
    /**
     * ?????????????????????????????????
     * @param dto
     * @param image
     * @param wmNews
     */
    public void saveRelationInfo(WmNewsDto dto,List<String> image,WmNews wmNews){
        List<String> images = dto.getImages();
        //????????????,???????????????news???image??????
        if(dto.getType().equals((short)-1)){
            //??????????????????3,???????????????
            if(image.size()>=WmNewsConstants.imageStatus.THREE.getCode()){
                wmNews.setType(WmNewsConstants.imageStatus.THREE.getCode());
               images = image.stream().limit(3).collect(Collectors.toList());
            }else if(image.size()>=WmNewsConstants.imageStatus.ONE.getCode()){
                //????????????3???,?????????
                wmNews.setType(WmNewsConstants.imageStatus.ONE.getCode());
                images =image.stream().limit(1).collect(Collectors.toList());
            }else {
                //??????
                wmNews.setType(WmNewsConstants.imageStatus.NONE.getCode());
            }
            //??????news?????????
            if(images!=null&&images.size()>0){
                wmNews.setImages(StringUtils.join(images,","));
            }
            //??????news
            this.updateById(wmNews);
        }
        //?????????????????????????????????
        //???????????????????????????1
        Short type = 1;
        saveRelationship(wmNews, images,type);
    }

    /**
     * ???????????????????????????
     * @param wmNews
     * @param images
     * @param type
     */
    public void saveRelationship(WmNews wmNews, List<String> images,Short type) {
        if(images!=null&&images.size()>0){
            //????????????????????????id
            LambdaQueryWrapper<WmMaterial> lqw = new LambdaQueryWrapper<>();
            lqw.in(WmMaterial::getUrl,images);
            List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(lqw);
            //????????????????????????
            if(wmMaterials==null || wmMaterials.size() == 0){
                //??????????????????   ????????????????????????????????????????????????????????????????????????????????????????????????
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }
            if(wmMaterials.size()!=images.size()){
                //????????????
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }
            List<Integer> ids = wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
            WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
            for (Integer id : ids) {
                wmNewsMaterial.setType(type);
                wmNewsMaterial.setNewsId(wmNews.getId());
                wmNewsMaterial.setMaterialId(id);
                wmNewsMaterial.setOrd((short) 0);
                wmNewsMaterialMapper.insert(wmNewsMaterial);
            }
        }
    }

    /**
     * ???????????????????????????????????????
     * @param dto
     * @param image
     * @param wmNews
     */
    public void saveRelationInfoContent(WmNewsDto dto,List<String> image,WmNews wmNews){
        //???????????????????????????0
        Short type = 0;
        //?????????????????????????????????
        saveRelationship(wmNews, image,type);
    }

    /**
     * ???????????????
     * @param wmNewsDto ??????
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto wmNewsDto) {
        //1.????????????
        if(wmNewsDto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        //2.????????????
        WmNews wmNews = this.getById(wmNewsDto.getId());
        //2.1????????????
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            //2.2????????????????????????
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"??????????????????????????????");
        }
        //3.????????????
        LambdaUpdateWrapper<WmNews> uw = new LambdaUpdateWrapper<>();
        uw.eq(wmNewsDto.getId()!=null,WmNews::getId,wmNewsDto.getId());
        uw.set(wmNewsDto.getEnable()!=null&&
                wmNewsDto.getEnable()>-1&&
                wmNewsDto.getEnable()<2,WmNews::getEnable,wmNewsDto.getEnable());
        //3.1??????????????????
        String reason = wmNewsDto.getEnable()==0?"??????????????????,?????????":"????????????";
        uw.set(WmNews::getReason,reason);
        this.update(uw);
        //4.????????????????????????,?????????????????????,????????????
        if(wmNews.getArticleId()!=null){
            wmNews.setEnable(wmNewsDto.getEnable());
            ObjectMapper mapper = new ObjectMapper();
            try {
                String wmNewsStr = mapper.writeValueAsString(wmNews);
                kafkaTemplate.send(MqConstants.WM_NEWS_UP_OR_DOWN_TOPIC,"wmNews",wmNewsStr).addCallback(new SuccessCallback<SendResult<String, String>>() {
                    //??????????????????
                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        log.info("??????????????????:{}",result.getRecordMetadata());
                    }
                }, new FailureCallback() {
                    //??????????????????
                    @Override
                    public void onFailure(@NotNull Throwable ex) {
                        log.error("??????????????????,???????????????:{}",ex.getMessage());
                    }
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    /**
     * ?????????????????????
     * ??????????????????????????????,????????????public??????,????????????spring bean
     * @Async ??????AOP??????,??????????????????????????????????????????
     * @param id  ????????????id
     * @param
     */
    @Override
    public void autoScanWmNews(Long id) {
        //1.?????????????????????
        WmNews wmNews = newsMapper.selectById(id);
        if(wmNews==null){
            throw new RuntimeException("???????????????");
        }
        //2.?????????????????????????????????????????????????????????
        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            //2.1???????????????????????????,?????????
            Map<String, Object> map = getImageOrText(wmNews);
            List<String> images = (List<String>) map.get("images");
            images.add(wmNews.getImages());
            String content = (String) map.get("content");
            //2.2??????????????????????????????
            if(!moderationSensitiveScan(content,wmNews)){
                return;
            }
            //2.3????????????,???????????????,??????
            if(!moderationImages(images, wmNews)){
                return;
            };
            //2.4????????????
            if(!moderationText(content,wmNews)){
                return;
            }
            ResponseResult result = saveArticle(wmNews);
            System.out.printf("???????????????????????????:{}",result.getErrorMessage());
            log.warn(result.getErrorMessage());
            if(result.getCode()!=200){
                throw new CustomException(AppHttpCodeEnum.SERVER_ERROR);
            }
            //3????????????,????????????????????????
            //3.1????????????id
            Long articleId = (Long) result.getData();
            wmNews.setPublishTime(new Date());
            wmNews.setArticleId(articleId);
            updateWmNews(wmNews, (short) 9,"????????????");
        }
    }
    /**
     * ????????????????????????
     * @param wmNews
     * @return
     */
    public ResponseResult saveArticle(WmNews wmNews){
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,dto);
        dto.setLayout(wmNews.getType());
        //????????????
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmChannel!=null){
            dto.setChannelName(wmChannel.getName());
        }
        dto.setWmUserId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        //????????????,???Id
        if(wmUser.getApAuthorId()!=null){
            dto.setAuthorId(wmUser.getApAuthorId());
        }
        //??????????????????
        if(wmUser.getName()!=null){
            dto.setAuthorName(wmUser.getName());
        }
        //??????id
        if(wmNews.getArticleId()!=null){
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());
        dto.setPublishTime(new Date());
        //7.4???ServeletRequestAttributes????????????????????????????????????session
        ResponseResult result = articleFeignClient.saveOrUpdate(dto);
        return result;
    }
    /**
     * ????????????????????????????????????
     * @param wmNews
     * @return
     */
    public Map<String,Object> getImageOrText(WmNews wmNews){
        //??????,???????????????
        StringBuilder sb = new StringBuilder();
        sb.append(wmNews.getTitle());
        //??????,????????????
        List<String> images = new ArrayList<>();
        Map<String,Object> resultMap = new HashMap<>();
        if(StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> maps = JSON.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                //????????????
                if("text".equals(map.get("type"))){
                    sb.append(map.get("value"));
                }
                if("image".equals(map.get("type"))){
                    images.add(map.get("value").toString());
                }
            }
        }
        //??????????????????
        //2.???????????????????????????
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        resultMap.put("content",sb.toString());
        resultMap.put("images",images);
        return resultMap;
    }
    /**
     * ?????????????????????
     * @param wmNews
     */
    public void saveOrUpdateWmNews(WmNews wmNews) {
        //????????????
        wmNews.setUserId(Math.toIntExact(UserThreadLocalUtils.get()));
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        //??????
        wmNews.setEnable((short)1);
        if(wmNews.getId() == null){
            //??????
            save(wmNews);
        }else {
            //??????
            //????????????????????????????????????
            LambdaQueryWrapper<WmNewsMaterial> lqw = new LambdaQueryWrapper<>();
            lqw.eq(WmNewsMaterial::getNewsId,wmNews.getId());
            wmNewsMaterialMapper.delete(lqw);
            updateById(wmNews);
        }

    }


    /**
     * ?????????????????????????????????
     * @param wmNews
     */
    public boolean moderationText(String content,WmNews wmNews){
        boolean flag=true;
        //????????????????????????0,????????????
        if(content.length()==0&&wmNews.getTitle().length()==0){
            return true;
        }
        //????????????
        try {
        Map map = greenTextScan.greeTextScan(content);
        if(map!=null){
            //block??????????????????
            if("block".equals(map.get("suggestion"))){
                flag=false;
                updateWmNews(wmNews, (short) 2,"??????????????????,?????????");
            }
            //???????????????
            if("review".equals(map.get("suggestion"))){
                flag=false;
                updateWmNews(wmNews, (short) 3,"????????????,????????????");
            }
            }
        }catch (Exception e){
            flag =false;
            e.printStackTrace();
        }
        return flag;
    }
    /**
     * ?????????????????????????????????
     * @param images
     * @return
     */
    public boolean moderationImages(List<String> images,WmNews wmNews){
        boolean flag = true;
        if(images==null||images.size()==0){
                return true;
        }
        //????????????
        List<String> imageList = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> list = new ArrayList<>();
        try {
        for (String s : imageList) {
            byte[] bytes = fileStorageService.downLoadFile(s);
            //???byte[]?????????butteredImage
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            //???????????????????????????
            BufferedImage read = ImageIO.read(inputStream);
            //??????????????????
            String content = tess4jClient.toOCR(read);
            //???????????????
            if(!moderationSensitiveScan(content, wmNews)){
                return false;
            }
            list.add(bytes);
        }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
        //????????????
        try {
            Map map = greenImageScan.imageScan(list);
            //OCR????????????

            if(map != null){
                //????????????
                if("block".equals(map.get("suggestion"))){
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "??????????????????");
                }
                //???????????????  ??????????????????
                if("review".equals(map.get("suggestion"))){
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "????????????????????????????????????");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * ????????????????????????
     * @param content
     * @param wmNews
     * @return
     */
    public boolean moderationSensitiveScan(String content,WmNews wmNews){
        boolean flag = true;
        //?????????????????????
        LambdaQueryWrapper<WmSensitive> lqw = new LambdaQueryWrapper<>();
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(lqw.select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        //?????????????????????,?????????Map??????
        SensitiveWordUtil.initMap(sensitiveList);
        Map<String, Integer> stringIntegerMap = SensitiveWordUtil.matchWords(content);
        if(stringIntegerMap.size()>0){
            //???????????????????????????
            updateWmNews(wmNews, (short) 2,"?????????????????????,?????????");
            flag=false;
        }
        return flag;
    }
    /**
     * ?????? ??????,??????
     * @param wmNews
     * @param status
     * @param str
     */
    public void updateWmNews(WmNews wmNews, short status, String str) {
        wmNews.setReason(str);
        wmNews.setStatus(status);
        this.updateById(wmNews);
    }

    /**
     * ??????????????????
     * @param imageList
     * @return
     */
    public Map<String,Object> imageScan(List<String> imageList){
        Map<String,Object> map = new HashMap<>();
        map.put("suggestion","pass");
        return map;
    }

    /**
     * ??????????????????
     * @param content
     * @return
     */
    public Map<String,Object> textScan(String content){
        Map<String,Object> map = new HashMap<>();
        map.put("suggestion","pass");
        return map;
    }
}
