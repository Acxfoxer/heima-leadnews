package com.heima.search.service;

import com.heima.model.article.dto.SearchDto;
import com.heima.model.common.dtos.ResponseResult;

/**
 * @author 18727
 */

public interface ApUserSearchService {
    /**
     * 添加搜索记录
     * @param keyWord
     */
    public void addApUserSearch(String keyWord);

    /**
     * 查询联想词
     * @param searchDto
     * @return
     */
    ResponseResult loadAssociateRecord(SearchDto searchDto);
    /**
     * 加载搜索记录
     * @param searchDto
     * @return
     */
    ResponseResult loadHistoryRecords(SearchDto searchDto);
}
