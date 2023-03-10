package com.heima.common.constants;


/**
 * mq队列常量
 * @author 18727
 */
public class MqConstants {
    public final static String TTL_QUEUE ="ttl.queue";
    public final static String TTL_EXCHANGE ="ttl.direct";
    public final static String TTL_KEY ="ttl.key";
    public final static String DL_EXCHANGE ="dl.direct";
    public final static String DL_QUEUE ="dl.queue";
    public final static String DL_KEY ="dl.key";
    public final static String DELAY_EXCHANGE ="delay.direct";
    public final static String DELAY_QUEUE ="delay.queue";
    public final static String DELAY_KEY ="delay.key";
    /**
     * kafka 监听上架下架主题
     */
    public static final String WM_NEWS_UP_OR_DOWN_TOPIC="wm.news.up.or.down.topic";
    /**
     * 监听文章保存
     */
    public static final String AP_ARTICLE_SAVE="ap.article.topic";
}
