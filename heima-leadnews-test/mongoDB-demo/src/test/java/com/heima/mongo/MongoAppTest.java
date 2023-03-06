package com.heima.mongo;

import com.heima.model.search.pojos.ApAssociateWords;
import com.heima.model.search.pojos.ApUserSearch;
import com.heima.mongo.Dao.ApAssociateDao;
import com.heima.mongo.Dao.ApUserSearchDao;
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
    @Resource
    ApAssociateDao apAssociateDao;
    @Test
    public void testMongoSave(){
        ApUserSearch apUserSearch= new ApUserSearch();
        apUserSearch.setId("123");
        apUserSearch.setKeyword("张三");
        apUserSearch.setUserId(1234L);
        apUserSearch.setCreatedTime(new Date());
        searchDao.save(apUserSearch);
    }

    @Test
    public void testMongoUpdate(){
        ApUserSearch apUserSearch= new ApUserSearch();
        apUserSearch.setId("123");
        apUserSearch.setKeyword("张三2");
        apUserSearch.setUserId(1234L);
        apUserSearch.setCreatedTime(new Date());
        searchDao.save(apUserSearch);
    }

    @Test
    public void testMongoTemplate(){
        List<ApUserSearch> apUserSearches = mongoTemplate.find(Query.query(Criteria.where("keyword")
                .is("张三2")), ApUserSearch.class);
        System.out.println(apUserSearches);
    }

    @Test
    public void testMongoTemplate1(){
        ApAssociateWords apAssociateWords = new ApAssociateWords();
        apAssociateWords.setId("12");
        apAssociateWords.setAssociateWords("游戏");
        apAssociateWords.setCreatedTime(new Date());
        apAssociateDao.save(apAssociateWords);
    }
}
