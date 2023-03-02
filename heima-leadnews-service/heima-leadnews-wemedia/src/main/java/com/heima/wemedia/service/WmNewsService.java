package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dto.WmNewsDto;
import com.heima.model.media.dto.WmNewsPageReqDto;
import com.heima.model.media.pojos.WmNews;


/**
 * @author 18727
 */
public interface WmNewsService extends IService<WmNews> {

    /**
     * 分页
     * @param dto 参数
     * @return
     */
    public PageResponseResult toPage(WmNewsPageReqDto dto);
    /**
     * 保存到草稿
     * @param dto
     * @return
     */
    ResponseResult submitToDraft(WmNewsDto dto);

    /**
     * 上架或下架
     * @param wmNews
     * @return
     */
    ResponseResult downOrUp(WmNews wmNews);

    /**
     * 自媒体文章审核
     * @param wmNews  自媒体文章id
     */
    public void autoScanWmNews(WmNews wmNews);
}
