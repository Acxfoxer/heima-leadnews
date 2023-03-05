package com.heima.common.constants;

/**
 * @author lee
 */
public class ScheduleConstants {
    /**
     * 消费状态,0表示待发送
     */
    public static final int SCHEDULED=0;
    /**
     * 消费状态,1表示已发送,待消费
     */
    public static final int EXECUTED=1;
    /**
     * 消费状态,2表示已消费
     */
    public static final int CONSUMED=2;
}
