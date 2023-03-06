package com.heima.common.constants;

import lombok.Data;

/**
 * @author 18727
 */
@Data
public class WmNewsConstants {
    public final static String  MODERATION_SUCCESS ="pass";
    public final static String  MODERATION_ERROR ="error";
    public enum imageStatus{
        /**
         * 当前状态
         -1 自动
         3 三张图
         0 无图
         1 一张图
         */
        AUTO((short)-1),ONE((short)1),THREE((short)3),NONE((short)0);
        final short code;
        imageStatus(short code){
            this.code = code;
        }
        public short getCode(){
            return this.code;
        }
    }

}
