package com.heima.schedule.special;

import com.heima.schedule.service.TaskInfoService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author 18727
 */
@Component
public class RegularRefresh {
    @Resource
    private TaskInfoService service;
    @Resource(name = "redission")
    private RedissonClient redissonClient;
    @Scheduled(cron = "0/4 * * * * ?" )
    @PostConstruct
    public void refresh(){
        //获取锁
        RLock lock = redissonClient.getLock(this.getClass().getName() + ".refresh()");
            boolean flag = lock.tryLock();
            if(flag){
                try{
                    System.out.println("----------redis分布式锁获取成功------------");
                    System.out.println("start-------4分钟定时任务开始刷新任务到mq中-----------");
                    service.refresh();
                    System.out.println("start-------4分钟定时任务开始刷新任务到mq中-----------");
                }finally {
                    //解锁
                    lock.unlock();
                }
            }else {
                System.out.println("获取锁失败");
            }
    }
}
