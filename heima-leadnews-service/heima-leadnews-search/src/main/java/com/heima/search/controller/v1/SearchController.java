package com.heima.search.controller.v1;

import com.heima.model.article.dto.SearchDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.Impl.SearchServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 18727
 */
@RestController
@RequestMapping("/api/v1/")
public class SearchController {
    final
    SearchServiceImpl searchService;
    final
    ApUserSearchService userSearchService;

    public SearchController(SearchServiceImpl searchService, ApUserSearchService userSearchService) {
        this.searchService = searchService;
        this.userSearchService = userSearchService;
    }

    /**
     * 自动补全功能
     * @param searchDto 接受的参数
     * @return
     */
    @PostMapping("/associate/search/")
    public ResponseResult associateSearch(@RequestBody SearchDto searchDto){
        return userSearchService.loadAssociateRecord(searchDto);
    }
    /**
     * 查询
     * @param searchDto 接受的参数
     * @return
     */
    @PostMapping("/article/search/search")
    public ResponseResult searchArticle(@RequestBody SearchDto searchDto){
        return searchService.searchArticle(searchDto);

    }
    /**
     * 加载热点信息
     * @param searchDto 接受的参数
     * @return
     */
    @PostMapping("hot_keywords/load")
    public ResponseResult loadHotArticle(@RequestBody SearchDto searchDto){
        return null;
    }
    /**
     * 加载历史信息
     * @param searchDto 接受的参数
     * @return
     */
    @PostMapping("history/load")
    public ResponseResult loadHistoryArticle(@RequestBody SearchDto searchDto){
        return userSearchService.loadHistoryRecords(searchDto);
    }
}
