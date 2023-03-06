package com.heima.search.service.Impl;

import com.heima.model.article.dto.SearchDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.pojos.ApAssociateWords;
import com.heima.model.search.pojos.ApUserSearch;
import com.heima.search.dao.mongo.ApUserSearchDao;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.common.UserThreadLocalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 18727
 */
@Service
public class ApUserSearchServiceImpl implements ApUserSearchService {
    @Resource
    ApUserSearchDao userSearchDao;
    @Resource
    MongoTemplate mongoTemplate;
    private static final Integer MAX_SIZE = 10;
    private static final Integer MAX_PAGE_SIZE = 20;
    /**
     * 添加搜索记录
     * @param keyWord
     */
    @Override
    public void addApUserSearch(String keyWord) {
        Long userId = UserThreadLocalUtils.get();
        //判断是否为空
        if(keyWord!=null&&userId!=null){
            //查询用户所有的搜索关键字
            List<ApUserSearch> list = userSearchDao.findByUserId(userId);
            //判断是否包含keyword
            List<ApUserSearch> collect = list.stream().filter(item -> item.getKeyword().equals(keyWord)).collect(Collectors.toList());
            if(collect.size()>0){
                collect.forEach(item->{
                    item.setCreatedTime(new Date());
                    userSearchDao.save(item);
                });
            }else {
                ApUserSearch userSearch = new ApUserSearch();
                userSearch.setUserId(userId);
                userSearch.setKeyword(keyWord);
                userSearch.setCreatedTime(new Date());
                //判断数量是否等于10
                if(list.size()>=MAX_SIZE){
                    //获取按照时间排序最底部的数据id,只保留前9个
                    Query query =Query.query(Criteria.where("userId").is(userId))
                            .with(Sort.by(Sort.Direction.DESC,"createdTime"))
                            .skip(9);
                    //获取排序结果第九个后面的集合
                    List<ApUserSearch> apUserSearches = mongoTemplate.find(query, ApUserSearch.class);
                    apUserSearches.forEach(apUserSearch -> {
                        mongoTemplate.remove(apUserSearch);
                    });
                    mongoTemplate.save(userSearch);
                }else {
                    //不包含插入数据
                    mongoTemplate.save(userSearch);
                }
            }
        }
    }

    /**
     * 查询联想词
     * @param searchDto
     * @return
     */
    @Override
    public ResponseResult loadAssociateRecord(SearchDto searchDto) {
       if(searchDto==null|| StringUtils.isBlank(searchDto.getSearchWords())){
           return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"错误请稍后再试");
       }
       //设置最多联想词20
        if(searchDto.getPageSize()>MAX_PAGE_SIZE){
            searchDto.setPageSize(MAX_PAGE_SIZE);
        }
        //模糊查询,正则表达式拼接
        Query query = Query.query(Criteria.where("associateWords").regex(".*?\\"+
                searchDto.getSearchWords()+".*"));
        query.limit(searchDto.getPageSize());
        List<ApAssociateWords> apAssociateWords = mongoTemplate.find(query, ApAssociateWords.class);
        return ResponseResult.okResult(apAssociateWords);
    }

    /**
     * 加载搜索记录
     * @param searchDto
     * @return
     */
    @Override
    public ResponseResult loadHistoryRecords(SearchDto searchDto) {
        Long userId = UserThreadLocalUtils.get();
        if(userId==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //设置查询条件
        Query query = Query.query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC,"createdTime"));
        List<ApUserSearch> apUserSearches = mongoTemplate.find(query,ApUserSearch.class);
        return ResponseResult.okResult(apUserSearches);
    }
}
