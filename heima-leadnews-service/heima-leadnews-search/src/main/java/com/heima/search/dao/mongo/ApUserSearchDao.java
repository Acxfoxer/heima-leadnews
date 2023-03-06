package com.heima.search.dao.mongo;

import com.heima.model.search.pojos.ApUserSearch;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 18727
 */
@Repository
public interface ApUserSearchDao extends MongoRepository<ApUserSearch,String> {
}
