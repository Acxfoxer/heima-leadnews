package com.heima.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


import java.io.Serializable;

@Component //申明将组件添加进容器中
@ConfigurationProperties(prefix = "minio") //文件上传 配置前缀file.oss
@Data
public class MinIOConfigProperties implements Serializable {
    private String accessKey;
    private String secretKey;
    //minIo桶名词
    private String bucket;
    //暴露的url
    private String endpoint;
    //读取路径
    private String readPath;
}
