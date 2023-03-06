package com.lee.kafka.test;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;


@SpringBootTest
public class KafkaTestApp {
    @Test
    public void testAysnc(){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.136.101:9092");
        properties.put(ProducerConfig.RETRIES_CONFIG,5);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        //确认机制
        properties.put(ProducerConfig.ACKS_CONFIG,0);
        //设置压缩机制
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,"gzip");
        //构建生产者对象
        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
        ProducerRecord<String,String> producerRecord=new ProducerRecord<>("topic-product",0,"product","打工人天下第一");
        System.out.println("12141");
        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(e != null){
                    System.out.println("记录异常信息到日志表中");
                }
                System.out.println(recordMetadata.offset());
                System.out.println(recordMetadata.partition());
            }
        });
        System.out.println("12141");
        producer.close();
    }

    @Test
    public void testConsumer(){
        //1.kafka配置信息
        Properties properties = new Properties();
        //kafka的链接地址
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.136.101:9092");
        //消费者组
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"group1");
        //消费者反序列化器
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        //2.构建消费者对象
        KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(properties);
        //3.订阅主题
        consumer.subscribe(Collections.singletonList("topic-product"));
        //4.线程循环监听内容
        while (true){
            //4.1设置0.5毫秒拉去一次
            ConsumerRecords<String,String> consumerRecords = consumer.poll(Duration.ofMillis(50));
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
