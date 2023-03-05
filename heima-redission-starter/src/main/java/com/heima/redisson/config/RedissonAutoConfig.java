package com.heima.redisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redission
 * @author lee
 */
@EnableConfigurationProperties(RedissonConfigProperties.class)
@Configuration
public class RedissonAutoConfig {

    private final RedissonConfigProperties properties;

    public RedissonAutoConfig(RedissonConfigProperties properties) {
        this.properties = properties;
    }

    @Bean("redission")
    public RedissonClient redissonClient(){

        Config config = new Config();
        config.useSingleServer().setAddress(properties.getServer())
                .setPassword(properties.getPassword());
        RedissonClient redisson = Redisson.create(config);
        return  redisson;
    }
}
