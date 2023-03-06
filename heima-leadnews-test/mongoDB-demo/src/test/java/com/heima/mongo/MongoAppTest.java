package com.heima.mongo;

import com.heima.mongo.Dao.ApUserSearchDao;
import com.heima.mongo.pojos.ApUserSearch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class MongoAppTest {
    @Resource
    ApUserSearchDao searchDao;
    @Resource
    private MongoTemplate mongoTemplate;
    @Test
    public void testMongoSave(){
        ApUserSearch apUserSearch= new ApUserSearch();
        apUserSearch.setId("123");
        apUserSearch.setKeyword("张三");
        apUserSearch.setUserId(1234);
        apUserSearch.setCreatedTime(new Date());
        searchDao.save(apUserSearch);
    }

    @Test
    public void testMongoUpdate(){
        ApUserSearch apUserSearch= new ApUserSearch();
        apUserSearch.setId("123");
        apUserSearch.setKeyword("张三2");
        apUserSearch.setUserId(1234);
        apUserSearch.setCreatedTime(new Date());
        searchDao.save(apUserSearch);
    }

    @Test
    public void testMongoTemplate(){
        List<ApUserSearch> apUserSearches = mongoTemplate.find(Query.query(Criteria.where("keyword")
                .is("张三2")), ApUserSearch.class);
        System.out.println(apUserSearches);
    }
}
