package com.lee.kafka.listener;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.model.media.pojos.WmNews;

import com.heima.model.media.pojos.WmUser;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author 18727
 */
@Component
public class ConsumerListener {
    @KafkaListener(topics = "topic")
    public void listenMessage(String msg){
        if(msg!=null&&!msg.equals("")){
            System.out.println(msg);
        }
    }
    @KafkaListener(topics = "WmNews-topic")
    public void listenObject(String msg){
        ObjectMapper objectMapper = new ObjectMapper();
        if(msg!=null&&!msg.equals("")){
            WmUser wmUser =null;
            try {
                wmUser = objectMapper.readValue(msg, WmUser.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }finally {
                System.out.println(wmUser);
            }
        }
    }
}
