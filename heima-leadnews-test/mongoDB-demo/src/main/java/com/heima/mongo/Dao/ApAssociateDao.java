package com.heima.mongo.Dao;


import com.heima.model.search.pojos.ApAssociateWords;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 18727
 */
@Repository
public interface ApAssociateDao extends MongoRepository<ApAssociateWords,String> {
}
