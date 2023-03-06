package com.heima.search.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.common.constants.MqConstants;
import com.heima.model.search.vos.SearchArticleVo;
import com.heima.search.service.Impl.SearchServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消息队列监听器
 * @author 18727
 */
@Component
public class MqListener {
    /**
     * 构造器注入
     */
    final SearchServiceImpl searchService;
    public MqListener(SearchServiceImpl searchService) {
        this.searchService = searchService;
    }
    @KafkaListener(topics = MqConstants.AP_ARTICLE_SAVE)
    public void onMessage(String msg){
        ObjectMapper objectMapper = new ObjectMapper();
        if(StringUtils.isNotBlank(msg)){
            try {
                SearchArticleVo searchArticleVo = objectMapper.readValue(msg, SearchArticleVo.class);
                if(searchArticleVo!=null){
                    searchService.addNews(searchArticleVo);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
