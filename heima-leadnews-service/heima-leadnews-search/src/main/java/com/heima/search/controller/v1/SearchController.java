package com.heima.search.controller.v1;

import com.heima.model.article.dto.SearchDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.search.service.Impl.SearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    SearchServiceImpl searchService;
    /**
     * 自动补全功能
     * @param searchDto 接受的参数
     * @return
     */
    @PostMapping("/associate/search/")
    public ResponseResult associateSearch(@RequestBody SearchDto searchDto){
        return null;
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
    @PostMapping("hot_keywords/loadHot")
    public ResponseResult loadHotArticle(@RequestBody SearchDto searchDto){
        return null;
    }
    /**
     * 加载历史信息
     * @param searchDto 接受的参数
     * @return
     */
    @PostMapping("hot_keywords/loadHistory")
    public ResponseResult loadHistoryArticle(@RequestBody SearchDto searchDto){
        return null;
    }

}
