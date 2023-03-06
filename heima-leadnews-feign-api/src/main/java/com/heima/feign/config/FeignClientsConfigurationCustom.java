package com.heima.feign.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 定义feign 设置
 * @author 18727
 */
@Component
public class FeignClientsConfigurationCustom implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
    // 此种方式是线程安全的
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
    // 不为空时取出请求中的header 原封不动的设置到feign请求中
        if (null != attributes) {
            //从Attribute尝试获取需要的值
            String token = (String) attributes.getAttribute("token", 1);
            String userId = (String) attributes.getAttribute("userId", 2);
            //判断token是否存在
            if(StringUtils.isNotBlank(token)){
                //存在token,设置请求头
                requestTemplate.header("token",token);
            }else if(StringUtils.isNotBlank(userId)){
                //存在userId,设置请求头
                requestTemplate.header("userId",userId);
            }else {
                //Attribute未携带需要的值,从请求中获取请求头
                HttpServletRequest request = attributes.getRequest();
                // 遍历设置 也可从request取出需要的Header 写到RequestTemplate 中
                String tokenHeader = request.getHeader("token");
                String userIdHeader = request.getHeader("userId");
                //判断token是否存在
                if (tokenHeader != null&&userIdHeader!=null) {
                    requestTemplate.header("token",tokenHeader);
                    requestTemplate.header("userId",userIdHeader);
                }
            }
        }
    }
}