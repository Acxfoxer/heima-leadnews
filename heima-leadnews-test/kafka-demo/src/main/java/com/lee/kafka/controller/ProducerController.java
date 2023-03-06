package com.lee.kafka.controller;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.model.media.pojos.WmUser;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author 18727
 */
@RestController
public class ProducerController {
    private final KafkaTemplate<String,String> template;

    public ProducerController(KafkaTemplate<String, String> template) {
        this.template = template;
    }

    /**
     * 发送消息不为对象
     * @return
     */
    @GetMapping("/hello")
    public String hello(){
        template.send("topic","草泥马,┏┛墓┗┓...(((m -__-)m,🥁,🐖");
        return "ok";
    }
    /**
     * 发送对象
     */
    @GetMapping("/object")
    public Object publishObject(){
        WmUser wmUser = new WmUser();
        wmUser.setApUserId(1613131L);
        wmUser.setApAuthorId(561122L);
        wmUser.setLoginTime(new Date());
        wmUser.setEmail("agagagaggaagga.com");
        wmUser.setLocation("重启");
        wmUser.setCreatedTime(new Date());
        ObjectMapper objectMapper = new ObjectMapper();
        String s;
        try {
             s = objectMapper.writeValueAsString(wmUser);
            template.send("WmNews-topic",s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return s;
    }
}
