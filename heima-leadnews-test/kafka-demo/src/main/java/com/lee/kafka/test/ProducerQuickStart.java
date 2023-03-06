package com.lee.kafka.test;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @author 18727
 */
public class ProducerQuickStart {
    public static void main(String[] args) {
        //1.kafka配置信息
        Properties properties = new Properties();
        //kafka的链接地址
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.136.101:9092");
        //发送失败,失败的重试次数
        properties.put(ProducerConfig.RETRIES_CONFIG,5);
        //消息的key序列化器
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        //消息value序列号器
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        //2.构建生产者对象
        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
        //2.1封装需要发送的消息
        ProducerRecord<String,String> record = new ProducerRecord<>("topic-demo",0,"token","傻逼隆军级");
        //3 发送消息
        producer.send(record);
        //4.关闭消息通道
        producer.close();
    }
}
