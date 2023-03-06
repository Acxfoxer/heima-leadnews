package com.heima.article.web.v1;

import com.heima.article.service.ApArticleService;
import com.heima.model.article.dto.ArticleDto;
import com.heima.model.article.dto.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.vos.SearchArticleVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/article")
@Api(value = "主页信息展示",tags = "all")
public class ArticleController {
    private final ApArticleService apArticleService;
    public ArticleController(ApArticleService apArticleService) {
        this.apArticleService = apArticleService;
    }

    /**
     * 加载新闻信息
     * @param
     * @return
     */
    @ApiOperation("默认模式")
    @PostMapping("/load")
    public ResponseResult<List<ApArticle>> load(@RequestBody ArticleHomeDto dto){
        System.out.println(dto);
        return apArticleService.load(dto);
    }

    /**
     * 加载更多信息
     * @param articleHomeDto
     * @return
     */
    @ApiOperation("加载更多")
    @PostMapping("/loadmore")
    public ResponseResult<List<ApArticle>> loadMore(@RequestBody ArticleHomeDto articleHomeDto){
        return apArticleService.load(articleHomeDto);
    }

    /**
     * 加载最新信息
     * @param articleHomeDto
     * @return
     */
    @ApiOperation("加载最新")
    @PostMapping("/loadnew")
    public ResponseResult<List<ApArticle>> loadNew(@RequestBody ArticleHomeDto articleHomeDto){
        return apArticleService.load(articleHomeDto);
    }

    /**
     * 保存文章
     * @param dto
     * @return
     */
    @ApiOperation("保存文章")
    @PostMapping("/save")
    public ResponseResult saveOrUpdate(@RequestBody ArticleDto dto){
        return apArticleService.saveOrUpdate(dto);
    }
    /**
     * 查询所有接口
     * @return  返回文章集合
     */
    @GetMapping("/listAll")
    public ResponseResult<List<SearchArticleVo>> getSearchArticleVo(){
        return  ResponseResult.okResult(apArticleService.getSearchArticleVo());
    }
}
