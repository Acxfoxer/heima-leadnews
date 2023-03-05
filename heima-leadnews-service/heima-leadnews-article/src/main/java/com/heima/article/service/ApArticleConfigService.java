package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.media.pojos.WmNews;

/**
 * @author 18727
 */
public interface ApArticleConfigService extends IService<ApArticleConfig> {
    /**
     * 上架或下架接口
     * @param wmNews 包含文章id跟上下架状态
     */
    void updateUpOrDownMsg(WmNews wmNews);
}
