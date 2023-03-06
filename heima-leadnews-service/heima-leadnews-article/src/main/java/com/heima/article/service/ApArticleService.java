package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dto.ArticleDto;
import com.heima.model.article.dto.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.pojos.WmNews;
import com.heima.model.search.vos.SearchArticleVo;

import java.util.List;

/**
 * @author 18727
 */
public interface ApArticleService extends IService<ApArticle> {
    /**
     * 根据参数加载文章列
     * @param dto      前端传递参数
     * @return
     */
     ResponseResult<List<ApArticle>> load(ArticleHomeDto dto);

    /**
     * 添加文章
     * @param dto
     * @return
     */
    ResponseResult saveOrUpdate(ArticleDto dto);
    /**
     * 查询文档接口
     * @return
     */
    List<SearchArticleVo> getSearchArticleVo();
}
