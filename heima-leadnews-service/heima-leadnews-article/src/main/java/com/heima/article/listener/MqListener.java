package com.heima.article.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.MqConstants;
import com.heima.model.media.pojos.WmNews;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 监听kafka信息
 * @author 18727
 */
@Component
public class MqListener {
    @Resource
    ApArticleConfigService service;

    @KafkaListener(topics = MqConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void onMessage(String msg){
        ObjectMapper objectMapper = new ObjectMapper();
        if(StringUtils.isNotBlank(msg)){
            try {
                WmNews wmNews = objectMapper.readValue(msg, WmNews.class);
                service.updateUpOrDownMsg(wmNews);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
