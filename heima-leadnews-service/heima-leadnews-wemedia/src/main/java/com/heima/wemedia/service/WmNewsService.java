package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dto.WmNewsDto;
import com.heima.model.media.dto.WmNewsPageReqDto;
import com.heima.model.media.pojos.WmNews;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


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
     * 本类调用本类异步方法,必须实现public方法,手动获取spring bean
     * @param id  消息id
     * @param
     */
    public void autoScanWmNews(Long id);
}
