package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WmNewsConstants;
import com.heima.common.exceptionHandle.CustomException;
import com.heima.feign.article.ArticleFeignClient;
import com.heima.minio.service.FileStorageService;
import com.heima.model.article.dto.ArticleDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.media.dto.WmNewsDto;
import com.heima.model.media.dto.WmNewsPageReqDto;
import com.heima.model.media.pojos.*;
import com.heima.utils.common.UserThreadLocalUtils;
import com.heima.wemedia.mapper.*;
import com.heima.wemedia.service.WmNewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author 18727
 */
@Service
@Transactional(rollbackFor = {CustomException.class})
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Autowired
    WmNewsMapper newsMapper;
    @Autowired
    WmNewsMaterialMapper wmNewsMaterialMapper;
    @Autowired
    WmChannelMapper wmChannelMapper;
    @Autowired
    WmMaterialMapper wmMaterialMapper;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    WmUserMapper wmUserMapper;
    @Resource
    ArticleFeignClient articleFeignClient;

    /**
     * 分页
     * @param dto 参数
     * @return
     */
    @Override
    public PageResponseResult toPage(WmNewsPageReqDto dto) {
        //检查参数
        dto.checkParam();
        IPage<WmNews> iPage = new Page<>(dto.getPage(),dto.getSize());
        //构造条件
        LambdaQueryWrapper<WmNews> lqw = new LambdaQueryWrapper<>();
        //文章状态
        lqw.eq(dto.getStatus()!=null,WmNews::getStatus,dto.getStatus());
        //根据标题模糊查询
        lqw.like(dto.getKeyword()!=null,WmNews::getTitle,dto.getKeyword());
        //根据频道查询
        lqw.eq(dto.getChannelId()!=null,WmNews::getChannelId,dto.getChannelId());
        //默认用户id
        lqw.eq(WmNews::getUserId,UserThreadLocalUtils.get());
        //时间范围查询
        if(dto.getBeginPubDate()!=null&&dto.getEndPubDate()!=null){
            lqw.between(WmNews::getPublishTime,dto.getBeginPubDate(),dto.getEndPubDate());
        }
        this.page(iPage,lqw);
        PageResponseResult result = new PageResponseResult(dto.getPage(),dto.getSize(), (int) iPage.getTotal());
        result.setData(iPage.getRecords());
        return result;
    }
    /**
     * 保存到草稿,或者提交审核
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitToDraft(WmNewsDto dto) {
        //0.条件判断
        if(dto == null || dto.getContent() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //1.保存或修改文章
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto,wmNews,"images");
        //2.分割图片路径
        if(dto.getImages()!=null&&dto.getImages().size()>0){
            String images = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(images);
        }
        //2.封面类型为自动,设置type为null
        if(dto.getType().equals((short)-1)){
            wmNews.setType(null);
        }
        //3.保存到news表中
        saveOrUpdateWmNews(wmNews);
        //4.如果是草稿,结束当前方法
        if(dto.getStatus().equals(WmNews.Status.NORMAL.getCode())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        //5.提交审核,保存内容图片与素材的关系
        //5.1获取内容图片信息
        List<Map> maps = JSON.parseArray(wmNews.getContent(), Map.class);
        List<String> image = new ArrayList<>();
        maps.forEach(map->{
            if("image".equals(map.get("type"))){
                image.add(map.get("value").toString());
            }
        });
        //6.建立文章内容图片与素材库关系
        saveRelationInfoContent(dto,image,wmNews);
        //6.1 建立文章封面图片与素材库关系
        saveRelationInfo(dto,image,wmNews);
        //7.调用自动审核方法
        autoScanWmNews(wmNews.getId());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    /**
     * 封面图片与素材建立联系
     * @param dto
     * @param image
     * @param wmNews
     */
    private void saveRelationInfo(WmNewsDto dto,List<String> image,WmNews wmNews){
        List<String> images = dto.getImages();
        //如果自动,则重新设置news的image属性
        if(dto.getType().equals((short)-1)){
            //文章图片大于3,最多取三个
            if(image.size()>=WmNewsConstants.imageStatus.THREE.getCode()){
                wmNews.setType(WmNewsConstants.imageStatus.THREE.getCode());
               images = image.stream().limit(3).collect(Collectors.toList());
            }else if(image.size()>=WmNewsConstants.imageStatus.ONE.getCode()){
                //图片小于3张,取一张
                wmNews.setType(WmNewsConstants.imageStatus.ONE.getCode());
                images =image.stream().limit(1).collect(Collectors.toList());
            }else {
                //无图
                wmNews.setType(WmNewsConstants.imageStatus.NONE.getCode());
            }
            //修改news的图片
            if(images!=null&&images.size()>0){
                wmNews.setImages(StringUtils.join(images,","));
            }
            //更新news
            this.updateById(wmNews);
        }
        //建立素材跟图片关联信息
        //封面图片引用类型为1
        Short type = 1;
        saveRelationship(wmNews, images,type);
    }

    /**
     * 保存内容与素材联系
     * @param wmNews
     * @param images
     * @param type
     */
    private void saveRelationship(WmNews wmNews, List<String> images,Short type) {
        if(images!=null&&images.size()>0){
            //根据图片查询素材id
            LambdaQueryWrapper<WmMaterial> lqw = new LambdaQueryWrapper<>();
            lqw.in(WmMaterial::getUrl,images);
            List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(lqw);
            //判断素材是否有效
            if(wmMaterials==null || wmMaterials.size() == 0){
                //手动抛出异常   第一个功能：能够提示调用者素材失效了，第二个功能，进行数据的回滚
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }
            if(wmMaterials.size()!=images.size()){
                //抛出异常
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
     * 保存内容图片与素材建立联系
     * @param dto
     * @param image
     * @param wmNews
     */
    private void saveRelationInfoContent(WmNewsDto dto,List<String> image,WmNews wmNews){
        //内容图片关联关系为0
        Short type = 0;
        //建立素材跟图片关联信息
        saveRelationship(wmNews, image,type);
    }

    /**
     * 上架或下架
     * @param wmNews
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNews wmNews) {
        UpdateWrapper<WmNews> uw = new UpdateWrapper<>();
        uw.eq(wmNews.getId()!=null,"id",wmNews.getId());
        uw.set(wmNews.getEnable()!=null,"enable",wmNews.getEnable());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 自媒体文章审核
     * @param id 自媒体文章id
     */
    @Async
    @Override
    public void autoScanWmNews(Integer id) {
        //1.查询自媒体文章
        WmNews wmNews = newsMapper.selectById(id);
        if(wmNews==null){
            throw new RuntimeException("文章不存在");
        }
        //判断文章状态是不是处于正在提交的状态中
        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            //从内容中获取纯文本,跟图片
            Map<String, Object> map = getImageOrText(wmNews);
            //审核图片,审核不通过,返回
            if(!moderationImages((List<String>) map.get("images"), wmNews)){
                return;
            };
            //审核文档
            if(!moderationText(wmNews.getContent(),wmNews)){
                return;
            }
            //远程调用article保存接口
            ResponseResult result = saveArticle(wmNews);
            if(result.getCode()!=200){
                throw new CustomException(AppHttpCodeEnum.SERVER_ERROR);
            }
            //审核成功,更新后台文章数据
            //获取文章id
            Long articleId = (Long) result.getData();
            wmNews.setPublishTime(new Date());
            wmNews.setArticleId(articleId);
            updateWmNews(wmNews, (short) 9,"审核成功");
        }

    }
    /**
     * 保存文章到服务端
     * @param wmNews
     * @return
     */
    private ResponseResult saveArticle(WmNews wmNews){
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,dto);
        dto.setLayout(wmNews.getType());
        //频道名字
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmChannel!=null){
            dto.setChannelName(wmChannel.getName());
        }
        //作者名字,与Id
        if(wmNews.getArticleId()!=null){
            dto.setAuthorId(wmNews.getArticleId());
        }
        //设置用户id
        dto.setWmUserId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser!=null){
            dto.setAuthorName(wmUser.getName());
        }
        //文章id
        if(wmNews.getArticleId()!=null){
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());
        dto.setPublishTime(new Date());
        ResponseResult result = articleFeignClient.saveOrUpdate(dto);
        return result;
    }
    /**
     * 获取文章图片或者文本信息
     * @param wmNews
     * @return
     */
    private Map<String,Object> getImageOrText(WmNews wmNews){
        //存储,纯文本内容
        StringBuilder sb = new StringBuilder();
        //存粹,图片地址
        List<String> images = new ArrayList<>();
        Map<String,Object> resultMap = new HashMap<>();
        if(StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> maps = JSON.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                //文本内容
                if("text".equals(map.get("type"))){
                    sb.append(map.get("value"));
                }
                if("image".equals(map.get("type"))){
                    images.add(map.get("value").toString());
                }
            }
        }
        //提取封面图片
        //2.提取文章的封面图片
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        resultMap.put("content",sb);
        resultMap.put("images",images);
        return resultMap;
    }
    /**
     * 保存或修改文章
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        //补全属性
        wmNews.setUserId(Math.toIntExact(UserThreadLocalUtils.get()));
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        //上架
        wmNews.setEnable((short)1);
        if(wmNews.getId() == null){
            //保存
            save(wmNews);
        }else {
            //修改
            //删除文章图片与素材的关系
            LambdaQueryWrapper<WmNewsMaterial> lqw = new LambdaQueryWrapper<>();
            lqw.eq(WmNewsMaterial::getNewsId,wmNews.getId());
            wmNewsMaterialMapper.delete(lqw);
            updateById(wmNews);
        }

    }



    /**
     * 调用阿里云文章审核接口
     * @param wmNews
     */
    public boolean moderationText(String content,WmNews wmNews){
        boolean flag=true;
        //标题跟内容长度为0,直接返回
        if(content.length()==0&&wmNews.getTitle().length()==0){
            return flag;
        }
        //审核文章
        try {
        Map<String, Object> map = textScan(content);
        if(map!=null){
            //block代表审核失败
            if("block".equals(map.get("suggestion"))){
                flag=false;
                updateWmNews(wmNews, (short) 2,"文章内容违规,请修改");
            }
            //不确定信息
            if("review".equals(map.get("suggestion"))){
                flag=false;
                updateWmNews(wmNews, (short) 3,"文章审核,违规存疑");
            }
            }
        }catch (Exception e){
            flag =false;
            e.printStackTrace();
        }
        return flag;
    }
    /**
     * 调用阿里云图片检测接口
     * @param images
     * @return
     */
    public boolean moderationImages(List<String> images,WmNews wmNews){
        boolean flag = true;
        if(images==null||images.size()==0){
                flag=false;
        }
        //图片去重
        List<String> imageList = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> list = new ArrayList<>();
        for (String s : imageList) {
            byte[] bytes = fileStorageService.downLoadFile(s);
            list.add(bytes);
        }
        //审核图片
        try {
            Map<String, Object> map = imageScan(imageList);
            if(map != null){
                //审核失败
                if("block".equals(map.get("suggestion"))){
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "图片内容违规");
                }
                //不确定信息  需要人工审核
                if("review".equals(map.get("suggestion"))){
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "无法自动识别图片是否违规");
                }
            }
            return flag;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新 状态,原因
     * @param wmNews
     * @param status
     * @param str
     */
    private void updateWmNews(WmNews wmNews, short status, String str) {
        wmNews.setReason(str);
        wmNews.setStatus(status);
        this.updateById(wmNews);
    }

    /**
     * 图片扫描接口
     * @param imageList
     * @return
     */
    private Map<String,Object> imageScan(List<String> imageList){
        Map<String,Object> map = new HashMap<>();
        map.put("suggestion","pass");
        return map;
    }

    /**
     * 图片扫描接口
     * @param content
     * @return
     */
    private Map<String,Object> textScan(String content){
        Map<String,Object> map = new HashMap<>();
        map.put("suggestion","pass");
        return map;
    }
}
