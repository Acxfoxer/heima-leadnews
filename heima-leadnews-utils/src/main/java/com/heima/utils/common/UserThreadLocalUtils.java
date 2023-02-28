package com.heima.utils.common;


public class UserThreadLocalUtils {
    static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static Long get(){
        return threadLocal.get();
    }
    public static void setUserID(Long id){
        threadLocal.set(id);
    }
    public static void remove(){
        threadLocal.remove();
    }
}
