package com.heima.wemedia.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池,替换SimpleAsyncTaskExecutor,解决真正意义上线程复用问题
 * @author lee
 */
@Slf4j
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {
    @Bean("LeeAsyncExecutor")
    public ThreadPoolTaskExecutor executor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        int corePoolSize = 10;
        //最大线程数
        int maxPoolSize=50;
        //等待队列容量
        int queueCapacity = 10;
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        //线程占满,处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //定义线程前缀
        String threadNamePrefix = "LeeAsyncExecutor-";
        executor.setThreadNamePrefix(threadNamePrefix);
        //等待所有位于等待任务完成再释放
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //使用自定义的跨线程的请求级别线程工厂类19   int awaitTerminationSeconds = 5;
        executor.setAwaitTerminationMillis(5);
        executor.initialize();
        return executor;
    }
    @Override
    public Executor getAsyncExecutor() {
        return executor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error(String.format("执行异步任务'%s'", method),ex);
    }
}
