package com.heima.common.exceptionHandle;

import com.heima.model.common.enums.AppHttpCodeEnum;

/**
 * 自定义异常类
 */

public class CustomException extends RuntimeException {

    private AppHttpCodeEnum appHttpCodeEnum;

    public CustomException(AppHttpCodeEnum appHttpCodeEnum){
        this.appHttpCodeEnum = appHttpCodeEnum;
    }

    public AppHttpCodeEnum getAppHttpCodeEnum() {
        return appHttpCodeEnum;
    }
}
