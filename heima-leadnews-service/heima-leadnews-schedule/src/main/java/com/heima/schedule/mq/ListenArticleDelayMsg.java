package com.heima.schedule.mq;

import com.heima.common.constants.MqConstants;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 监听延迟信息
 * @author 18727
 */
@Component
public class ListenArticleDelayMsg {
    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(name = MqConstants.DELAY_QUEUE,autoDelete = "false"),
            exchange = @Exchange(name = MqConstants.DELAY_EXCHANGE,autoDelete = "false",durable = "true",delayed = "true"),
            key = MqConstants.DELAY_KEY))
    public void listen(String msg){
        System.out.println(msg);
    }
}
