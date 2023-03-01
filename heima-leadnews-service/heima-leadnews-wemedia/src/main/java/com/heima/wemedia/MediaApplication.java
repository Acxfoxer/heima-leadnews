package com.heima.wemedia;

import com.heima.feign.article.ArticleFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author 18727
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableFeignClients(basePackages = "com.heima.feign.article",clients = {ArticleFeignClient.class})
public class MediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaApplication.class);
    }
}
