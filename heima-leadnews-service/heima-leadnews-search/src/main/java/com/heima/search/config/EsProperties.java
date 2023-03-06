package com.heima.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * es 参数
 * @author 18727
 */
@ConfigurationProperties(prefix = "spring.elasticsearch")
@Data
public class EsProperties {
    private String uris;
    private String username;
    private String password;
}
