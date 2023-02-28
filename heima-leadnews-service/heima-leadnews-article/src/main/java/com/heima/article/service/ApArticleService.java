package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dto.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;

import java.util.List;

public interface ApArticleService extends IService<ApArticle> {
    /**
     * 根据参数加载文章列
     * @param dto      前端传递参数
     * @param
     */
     ResponseResult<List<ApArticle>> load(ArticleHomeDto dto);

}
