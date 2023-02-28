package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exceptionHandle.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.media.dto.WmNewsDto;
import com.heima.model.media.dto.WmNewsPageReqDto;
import com.heima.model.media.pojos.WmMaterial;
import com.heima.model.media.pojos.WmNews;
import com.heima.model.media.pojos.WmNewsMaterial;
import com.heima.utils.common.UserThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Autowired
    WmNewsMapper newsMapper;
    @Autowired
    WmNewsMaterialMapper wmNewsMaterialMapper;
    @Autowired
    WmMaterialMapper wmMaterialMapper;

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
        List<String> image = getImage(dto.getContent());
        //6.建立文章内容图片与素材库关系
        saveRelationInfoContent(dto,image,wmNews);
        //6.1 建立文章封面图片与素材库关系
        saveRelationInfo(dto,image,wmNews);
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
            if(image.size()>=3){
                wmNews.setType((short) 3);
               images = image.stream().limit(3).collect(Collectors.toList());
            }else if(image.size()>=1){
                //图片小于3张,取一张
                wmNews.setType((short) 1);
                images =image.stream().limit(1).collect(Collectors.toList());
            }else {
                //无图
                wmNews.setType((short) 0);
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
     * 获取文章图片
     * @param content
     * @return
     */
    private List<String> getImage(String content){
        List<String> materials = new ArrayList<>();
        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if(map.get("type").equals("image")){
                String imgUrl = (String) map.get("value");
                materials.add(imgUrl);
            }
        }
        return materials;
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
        wmNews.setEnable((short)1);//默认上架
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
}
