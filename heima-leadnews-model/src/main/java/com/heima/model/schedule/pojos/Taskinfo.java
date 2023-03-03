package com.heima.model.schedule.pojos;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author itheima
 */
@Data
@TableName("taskinfo")
public class Taskinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long taskId;

    /**
     * 执行时间
     */
    @TableField("execute_time")
    private Date executeTime;

    /**
     * 参数  json ，即要发送的消息
     */
    @TableField("parameters")
    private String parameters;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 任务类型
     */
    @TableField("task_type")
    private Integer taskType;

    /**
     *  任务状态
     *      0 ： 初始化
     *      1 :  已执行
     */
    @TableField("status")
    private Integer status;


    /**
     *  rabbitmq的交换机名称
     */
    @TableField("mq_exchange")
    private String  mqExchange;

    /**
     * 消息路由的key
     */
    @TableField("mq_routing_key")
    private String mqRoutingKey;

    @Version
    private Integer version;
}