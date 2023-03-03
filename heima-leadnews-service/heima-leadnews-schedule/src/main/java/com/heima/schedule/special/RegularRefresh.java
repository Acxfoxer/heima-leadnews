package com.heima.schedule.special;

import com.heima.schedule.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author 18727
 */
@Component
public class RegularRefresh {
    @Autowired
    private TaskInfoService service;

    @Scheduled(cron = "0/4 * * * * ?" )
    @PostConstruct
    public void refresh(){
        System.out.println("start-------4分钟定时任务开始刷新任务到mq中-----------");
        service.refresh();
        System.out.println("start-------4分钟定时任务开始刷新任务到mq中-----------");
    }
}
