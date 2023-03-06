package com.heima.search.dao.es;

import com.heima.model.article.pojos.ApArticle;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 18727
 */
@Repository
public interface ArticleDao extends ElasticsearchRepository<ApArticle,Long> {
}
