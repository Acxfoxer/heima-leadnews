package com.heima.search.service;

import com.heima.model.article.dto.SearchDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.vos.SearchArticleVo;

/**
 * @author 18727
 */
public interface SearchService {
    /**
     * 查询文章-分页检索
     * @param searchDto 查询条件类
     * @return  返回结果
     */
    ResponseResult searchArticle(SearchDto searchDto);

    /**
     * 添加数据到es
     * @param searchArticleVo
     * @return
     */
    ResponseResult addNews(SearchArticleVo searchArticleVo);
}
