package com.heima.common.interceptor;

import com.heima.model.media.pojos.WmUser;
import com.heima.utils.common.UserThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Optional;


@Component
@Slf4j
public class MyInterceptor implements HandlerInterceptor {
    /**
     * 限制访问
     * @param request  请求
     * @param response 响应
     * @param handler  处理
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断请求类是不是swagger的控制器,直接放行
        if(handler.getClass().getName().equals("springfox.documentation.swagger.web.ApiResourceController")){
            return true;
        }
        Enumeration<String> token = request.getHeaders("token");
        if(!token.hasMoreElements()){
            log.warn("未登录,无法访问!,请求路径{}",request.getRequestURI());
            response.setStatus(403);
            return false;
        }
        String userId = request.getHeader("userId");
        System.out.println(userId);
        if(!userId.equals("null")){
            //把用户id存入threadloacl中
            UserThreadLocalUtils.setUserID(Long.valueOf(userId));
            log.info("wmTokenFilter设置用户Id到threadlocal中...");
        }
        return true;
    }
    //controller后执行,异常情况下不会执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    //页面渲染后,用于资源清理,在controller后执行,再异常情况下也会执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String userId = request.getHeader("userId");
        Optional<String> optional = Optional.ofNullable(userId);
        if(optional.isPresent()){
            //存在则移除
            UserThreadLocalUtils.remove();
        }
    }
}
