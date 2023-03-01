package com.heima.feign.article;

import com.heima.feign.config.FeignClientsConfigurationCustom;
import com.heima.model.article.dto.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author 18727
 */
@FeignClient(value = "article-service",configuration = FeignClientsConfigurationCustom.class)
public interface ArticleFeignClient {
    /**
     * 远程调用接口
     * @param dto
     * @return 返回Article id
     */
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveOrUpdate(@RequestBody ArticleDto dto);
}
