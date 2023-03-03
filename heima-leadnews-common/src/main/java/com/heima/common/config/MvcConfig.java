package com.heima.common.config;

import com.heima.common.interceptor.MyInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 18727
 */
public class MvcConfig implements WebMvcConfigurer {
    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 对swagger的请求不进行拦截
        String[] excludePatterns = new String[]{"/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**",
                "/api", "/api-docs", "/api-docs/**", "/doc.html/**","/favicon.ico/**","/login/**","/api/v1/schedule/**",
        "/api/v1/article/save"};
        registry.addInterceptor(new MyInterceptor())
                //拦截指定路径请求
                .addPathPatterns("/**")
                //排除指定请求
                .excludePathPatterns(excludePatterns);
    }
}
