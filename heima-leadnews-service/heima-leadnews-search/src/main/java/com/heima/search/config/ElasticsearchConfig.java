package com.heima.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ElasticsearchClient 客户端配置类
 * @author 18727
 */
@Configuration
public class ElasticsearchConfig {
    @Autowired
    EsProperties esProperties;

    @Bean(value = "client")
    public ElasticsearchClient elasticsearchClient() {
        return new ElasticsearchClient(getTransport());
    }

    @Bean(value ="AsyncClient")
    public ElasticsearchAsyncClient elasticsearchAsyncClient() {
        return new ElasticsearchAsyncClient(getTransport());
    }
    /**
     * ElasticsearchClient 账户密码,host,设置
     * @return 返回transport
     */
    public ElasticsearchTransport getTransport(){
        HttpHost httpHost = HttpHost.create(esProperties.getUris());
        RestClient restClient = RestClient.builder(httpHost)
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                    //设置账号密码
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    //es账号密码
                    credentialsProvider.setCredentials(AuthScope.ANY, new
                            UsernamePasswordCredentials(esProperties.getUsername(), esProperties.getPassword()));
                    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpAsyncClientBuilder;
                }).build();
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }
}
