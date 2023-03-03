package com.heima.schedule;

import com.heima.common.constants.MqConstants;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@SpringBootTest
public class TestApp {
    @Resource
    RabbitTemplate template;
    @Test
    public void test(){
        Message msg = MessageBuilder.withBody("测试延迟队列,10秒".getBytes(StandardCharsets.UTF_8))
                .setHeader("x-delay", 10000)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        CorrelationData correlationData = new CorrelationData("1");
        template.convertAndSend(MqConstants.DELAY_EXCHANGE,MqConstants.DELAY_KEY,msg,correlationData );
    }
}
