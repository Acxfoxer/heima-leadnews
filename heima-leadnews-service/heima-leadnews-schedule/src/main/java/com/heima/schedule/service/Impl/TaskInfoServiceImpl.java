package com.heima.schedule.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.ScheduleConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author 18727
 */
@Service
@Slf4j
public class TaskInfoServiceImpl extends ServiceImpl<TaskinfoMapper,Taskinfo> implements TaskInfoService {
    @Resource
    private RabbitTemplate template;

    /**
     * 添加任务进数据库,持久化
     *
     */
    @Override
    public ResponseResult addTask(Taskinfo taskinfo) {
        if(taskinfo!=null){
        //获取未来五分钟时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        this.save(taskinfo);
        //大于则不发送消息
        if(taskinfo.getExecuteTime().after(calendar.getTime())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        Message message;
        long delayTime = 0;
        //判断发布时间是否小于当前时间
        if(taskinfo.getExecuteTime().after(new Date())){
            //大于当前时间,小于当前时间+5分钟,发送延迟消息
            delayTime= calendar.getTimeInMillis()-taskinfo.getExecuteTime().getTime();
        }
            byte[] bytes = taskinfo.getTaskId().toString().getBytes(StandardCharsets.UTF_8);
            message = MessageBuilder.withBody(bytes)
                .setHeader("x-delay", delayTime)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        //发送消息
            //设置请求头
            MessagePostProcessor messagePostProcessor = message1 -> {
                message1.getMessageProperties().setHeader("token","Delay-Publish-business");
                return message1;
            };
        template.convertAndSend(taskinfo.getMqExchange(),taskinfo.getMqRoutingKey(),message);
        //发送消息后修改任务状态,
            taskinfo.setStatus(1);
            this.updateById(taskinfo);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
    /**
     * 定时查询数据库,发送延迟消息,查询范围是当日零点到每次业务执行时间后五分钟
     */
    @Override
    public void refresh() {
        //获取截止时间戳
        Calendar deadLine = Calendar.getInstance();
        deadLine.add(Calendar.MINUTE,5);
        //获取今日零点时间戳
        Calendar toDayZeroTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        //设置小时为0
        toDayZeroTime.set(Calendar.HOUR_OF_DAY,0);
        //设置分钟为0
        toDayZeroTime.set(Calendar.MINUTE,0);
        //设置秒为0
        toDayZeroTime.set(Calendar.SECOND,0);
        //设置毫秒为零
        toDayZeroTime.set(Calendar.MILLISECOND,0);
        //构造查询条件
        LambdaQueryWrapper<Taskinfo> lqw = new LambdaQueryWrapper<>();
        //大于等于零点时间戳
        lqw.ge(Taskinfo::getExecuteTime,toDayZeroTime);
        //小于等于截至的时间
        lqw.le(Taskinfo::getExecuteTime,deadLine);
        //状态为0
        lqw.eq(Taskinfo::getStatus, ScheduleConstants.SCHEDULED);
        List<Taskinfo> list = this.list(lqw);
        //设置regular请求头,
        String regularToken= "Delay-Publish-business";
        if(list.size()>0){
            long delayTime;
            for (Taskinfo taskinfo : list) {
                delayTime=taskinfo.getExecuteTime().getTime()-(new Date()).getTime();
                Message msg = MessageBuilder.withBody(taskinfo.getTaskId().toString().getBytes(StandardCharsets.UTF_8))
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                        .setHeader("x-delay",delayTime)
                        .build();
                //设置请求头
                MessagePostProcessor messagePostProcessor = message1 -> {
                    message1.getMessageProperties().setHeader("token",regularToken);
                    return message1;
                };
                template.convertAndSend(taskinfo.getMqExchange(),taskinfo.getMqRoutingKey(),msg,messagePostProcessor);
                //修改状态
                taskinfo.setStatus(ScheduleConstants.EXECUTED);
                this.updateById(taskinfo);
            }
        }
    }
}
