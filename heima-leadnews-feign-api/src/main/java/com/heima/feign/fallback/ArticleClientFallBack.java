package com.heima.feign.fallback;

import com.heima.feign.article.ArticleFeignClient;
import com.heima.model.article.dto.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义降级类
 * @author 18727
 */
@Slf4j
@Component
public class ArticleClientFallBack implements FallbackFactory<ArticleFeignClient> {
    @Override
    public ArticleFeignClient create(Throwable cause) {
        log.error("异常原因:{}", cause.getMessage(), cause);
        System.out.println("飒飒发嘎嘎嘎嘎嘎给:12234555  ");
        return new ArticleFeignClient() {
            @Override
            public ResponseResult saveOrUpdate(ArticleDto dto) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"调用失败,服务降级");
            }

            @Override
            public ResponseResult<List<SearchArticleVo>> listAll() {
                ResponseResult<List<SearchArticleVo>> result = new ResponseResult<>();
                result.setData(null);
                result.setCode(AppHttpCodeEnum.SERVER_ERROR.getCode());
                result.setErrorMessage("查询失败");
                return result;
            }
        };
    }
}
