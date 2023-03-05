package com.heima.redisson.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 18727
 */
@Data
@ConfigurationProperties(prefix = "redisson")
public class RedissonConfigProperties {

    private String server;
    private String password;
}
