package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.media.pojos.WmNews;
import org.springframework.stereotype.Service;

/**
 * ApArticleConfig 业务层代码
 * @author 18727
 */
@Service
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {
    /**
     * 上架或下架接口
     * @param wmNews 包含文章id跟上下架状态
     */
    @Override
    public void updateUpOrDownMsg(WmNews wmNews) {
        if(wmNews!=null){
            //ApArticleConfig 的 isDown的0上架,1下架
            Integer isDown = 0;
            //WmNews enable 中enable=0表示下架,1表示上架
            if(wmNews.getEnable()==0){
                isDown=1;
            }
            LambdaUpdateWrapper<ApArticleConfig> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(ApArticleConfig::getArticleId,wmNews.getArticleId())
                    .set(ApArticleConfig::getIsDown,isDown);
            this.update(lambdaUpdateWrapper);
        }
    }
}
