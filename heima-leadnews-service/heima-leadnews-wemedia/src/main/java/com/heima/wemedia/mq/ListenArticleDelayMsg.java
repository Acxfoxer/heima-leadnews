package com.heima.wemedia.mq;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.MqConstants;
import com.heima.common.constants.ScheduleConstants;
import com.heima.feign.article.TaskInfoClient;
import com.heima.model.media.pojos.WmNews;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
/**
 * 监听延迟信息
 * @author 18727
 */
@Component
@Slf4j
public class ListenArticleDelayMsg {
    @Resource
    WmNewsService wmNewsService;
    @Resource
    TaskInfoClient client;
    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(name = MqConstants.DELAY_QUEUE,autoDelete = "false"),
            exchange = @Exchange(name = MqConstants.DELAY_EXCHANGE,delayed = "true"),
            key = MqConstants.DELAY_KEY))
    public void listen(@Payload String taskId,
                       @Header("token")Object token){
        //获取请求上下文
        if(StringUtils.isNotBlank(taskId)){
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(requestAttributes!=null){
                requestAttributes.setAttribute("token",token,1);
            }
            Taskinfo taskinfo = client.selectTaskById(Long.valueOf(taskId));
            //判断任务状态是不是等于1,等于1表示已经消息发送那个但未消费
            if(taskinfo.getStatus()== ScheduleConstants.EXECUTED){
                WmNews wmNews = JSON.parseObject(taskinfo.getParameters(), WmNews.class);
                //调用自动审核接口
                wmNewsService.autoScanWmNews(Long.valueOf(wmNews.getId()));
                //审核结束更新状态
                taskinfo.setStatus(ScheduleConstants.CONSUMED);
                client.updateTask(taskinfo);
            }else {
                log.warn("接收到重复消息,具体信息为{}", JSON.toJSONString(taskinfo));
            }
        }

    }
}
