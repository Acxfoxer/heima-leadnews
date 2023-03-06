package com.lee.kafka.test;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * 测试kafka消费者
 * @author 18727
 */

public class ConsumerQuickStart {
    public static void main(String[] args) {
        //1.kafka配置信息
        Properties properties = new Properties();
        //kafka的链接地址
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.136.101:9092");
        //消费者组
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"group");
        //消费者反序列化器
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        //2.构建消费者对象
        KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(properties);
        //3.订阅主题
        consumer.subscribe(Collections.singletonList("topic-demo"));
        //4.线程循环监听内容
        while (true){
            //4.1设置0.5毫秒拉去一次
            ConsumerRecords<String,String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
            //4.2获取信息
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                //4.2.1输出key
                System.out.println(consumerRecord.key());
                //4.2.2输出value
                System.out.println(consumerRecord.value());
                //4.2.3输出信息所在分区的分区号
                System.out.println(consumerRecord.partition());
                //4.2.4输出分区中消息的偏移量,分区ha
                System.out.println(consumerRecord.offset());
            }
        }
    }
}
