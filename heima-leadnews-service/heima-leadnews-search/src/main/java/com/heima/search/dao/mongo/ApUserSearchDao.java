package com.heima.search.dao.mongo;

import com.heima.model.search.pojos.ApUserSearch;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 18727
 */
@Repository
public interface ApUserSearchDao extends MongoRepository<ApUserSearch,String> {
    /**
     * 根据用户id查询
     * @param userId
     * @return
     */
    List<ApUserSearch> findByUserId(Long userId);

    /**
     * 根据用户id跟关键字查询
     * @param keyword
     * @param userId
     * @return
     */
    List<ApUserSearch> findByUserIdAndKeyword(Long userId, String keyword);
}
