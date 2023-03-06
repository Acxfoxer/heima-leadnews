package com.heima.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.heima.feign.article.ArticleFeignClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootTest
@Slf4j
public class SearchAppTest {
    @Resource(name = "client")
    private ElasticsearchClient elasticsearchClient;
    @Resource
    ArticleFeignClient feignClient;

    @Test
    public void test(){
        //Date = new Date(1618762817000L);   //2021-04-19 00:20:19
        Date date = new Date(1599489079000L);      //2020-09-07 22:31:07
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
        String format = sim.format(date);
        System.out.println(format);
    }

    /**
     * 插入全部数据
     * @throws IOException 抛出异常
     */
    @Test
    public void insertEs() throws IOException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes!=null){
            requestAttributes.setAttribute("token","search",1);
        }
        ResponseResult<List<SearchArticleVo>> result = feignClient.listAll();
        List<SearchArticleVo> list = result.getData();
        if(list!=null){
            BulkRequest.Builder br = new BulkRequest.Builder();
            for (SearchArticleVo searchArticleVo : list) {
                br.operations(op->op
                        .index(i->i
                                .index("app_info_article")
                                .id(searchArticleVo.getId().toString())
                                .document(searchArticleVo)));
            }
            BulkResponse response = elasticsearchClient.bulk(br.build());
            //日志记录
            if (response.errors()) {
                log.error("Bulk had errors");
                for (BulkResponseItem item: response.items()) {
                    if (item.error() != null) {
                        log.error(item.error().reason());
                    }
                }
            }
        }
    }
}
