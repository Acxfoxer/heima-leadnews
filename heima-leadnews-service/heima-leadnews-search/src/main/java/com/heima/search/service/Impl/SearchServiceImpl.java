package com.heima.search.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.heima.model.article.dto.SearchDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVo;
import com.heima.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author 18727
 */
@Service
@Slf4j
public class SearchServiceImpl implements SearchService {
    @Resource(name = "client")
    ElasticsearchClient esClient;
    /**
     * 查询文章-分页检索
     * @param searchDto 查询条件类
     * @return 返回结果
     */
    @Override
    public ResponseResult searchArticle(SearchDto searchDto) {
        if(searchDto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }
        //构建布尔查询
        BoolQuery.Builder bool = new BoolQuery.Builder();
        //查询字段是否为空
        if(searchDto.getSearchWords()!=null){
            bool.must(q->q.match(m -> m.field("title").query(searchDto.getSearchWords())));
        }
        //时间条件
        bool.filter(f->f.range(r->r.field("publishTime")
                .lt(JsonData.of(searchDto.getMinBehotTime()))));
        try {
            SearchResponse<SearchArticleVo> response = esClient.search(s -> s.index("app_info_article")
                            .query(bool.build()._toQuery())
                            //排序查询,默认按时间倒序查询
                            .sort(ss->ss.field(f->f
                                    .field("publishTime")
                                    .order(SortOrder.Desc)))
                            .highlight(h->h.fields("title", HighlightField.of(high->high
                                    .preTags("<font style='color: red; font-size: inherit;'>")
                                    .postTags("</font>"))))
                            .from(searchDto.getPageNum())
                            .size(searchDto.getPageSize())
                    , SearchArticleVo.class);
            //结果中获取对象
            List<SearchArticleVo> result = getResult(response);
            return ResponseResult.okResult(result);
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }
    }

    /**
     * 添加数据到es
     *
     * @param searchArticleVo
     * @return
     */
    @Override
    public ResponseResult addNews(SearchArticleVo searchArticleVo) {
        if(searchArticleVo==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        try {
            IndexResponse indexResponse = esClient.index(i -> i
                    .index("app_info_article")
                    .id(searchArticleVo.getId().toString())
                    .document(searchArticleVo));
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 结果封装
     * @param response
     */
    public List<SearchArticleVo> getResult(SearchResponse<SearchArticleVo> response){
        List<Hit<SearchArticleVo>> hits = response.hits().hits();
        List<SearchArticleVo> list = new ArrayList<>();
        for (Hit<SearchArticleVo> hit : hits) {
            SearchArticleVo searchArticleVo = new SearchArticleVo();
            if(hit.source()!=null) {
                BeanUtils.copyProperties(hit.source(),searchArticleVo);
            }
            if(hit.highlight()!=null&&hit.highlight().size()>0){
                String title = StringUtils.join(hit.highlight().get("title"));
                if(title.contains("[")||title.contains("]")){
                    String newTitle = title.substring(1, title.length() - 1);
                    searchArticleVo.setH_title(newTitle);
                }else {
                    searchArticleVo.setH_title(title);
                }
            }
            list.add(searchArticleVo);
        }
        return list;
    }
}
