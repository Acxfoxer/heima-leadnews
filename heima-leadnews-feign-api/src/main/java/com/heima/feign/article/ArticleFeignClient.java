package com.heima.feign.article;

import com.heima.feign.config.FeignClientsConfigurationCustom;
import com.heima.feign.fallback.ArticleClientFallBack;
import com.heima.model.article.dto.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.vos.SearchArticleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author 18727
 */
@FeignClient(value = "article-service",configuration = FeignClientsConfigurationCustom.class
        ,fallbackFactory =ArticleClientFallBack.class )
public interface ArticleFeignClient {
    /**
     *远程调用接口
     * @param   dto 传入参数
     * @return  返回Article id
     */
    @PostMapping("/api/v1/article/save")
    ResponseResult saveOrUpdate(@RequestBody ArticleDto dto);

    /**
     * es检索模块查询文章接口
     * @return 返回文章集合
     */
    @GetMapping("/api/v1/article/listAll")
    public ResponseResult<List<SearchArticleVo>> listAll();
}
