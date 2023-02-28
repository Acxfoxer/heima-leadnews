package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dto.WmNewsDto;
import com.heima.model.media.dto.WmNewsPageReqDto;
import com.heima.model.media.pojos.WmNews;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1")
public class NewsController {
    @Autowired
    WmNewsService wmNewsService;

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/news/list")
    public PageResponseResult getList(@RequestBody WmNewsPageReqDto dto){
        return  wmNewsService.toPage(dto);
    }

    /**
     * 保存到草稿,或提交审核
     * @param dto
     * @return
     */
    @PostMapping("/news/submit")
    public ResponseResult submit(@RequestBody WmNewsDto dto){
        return wmNewsService.submitToDraft(dto);
    }
    /**
     * 下架
     * @param
     * @return
     */
    @PostMapping("/news/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNews wmNews){
        return wmNewsService.downOrUp(wmNews);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/news/one/{id}")
    public ResponseResult Update(@PathVariable("id")Long id){
        WmNews wmNews = wmNewsService.getById(id);
        return ResponseResult.okResult(wmNews);
    }
}
