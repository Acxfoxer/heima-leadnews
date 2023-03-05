package com.lee.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author 18727
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class KafkaApp {
    public static void main(String[] args) {
        SpringApplication.run(KafkaApp.class);
    }
}
