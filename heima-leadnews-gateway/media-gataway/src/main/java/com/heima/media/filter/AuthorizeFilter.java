package com.heima.media.filter;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.heima.utils.common.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1获取request和response
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //2.判断是否是登录
        if(request.getURI().getPath().contains("/login")){
            return chain.filter(exchange);
        }
        //3.获取token
        String token = request.getHeaders().getFirst("token");
        //4.判断token是否存在
        if(StringUtils.isBlank(token)){
            //如果token不存在,响应校验失败
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //5.判断token是否有效
        try {
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            //5.1 验证token是否过期
            if(claimsBody==null){
                //返回
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                //终止请求方法
                return response.setComplete();
            }
            int result = AppJwtUtil.verifyToken(claimsBody);
            if(result==0||result==-1){
                //转发用户信息
                String userId = claimsBody.get("id").toString();
                //构建请求
                ServerHttpRequest serverHttpRequest = request.mutate()
                        .header("userId",userId).build();
                ServerWebExchange webExchange = exchange.mutate().request(serverHttpRequest).build();
                //放行请求
                return chain.filter(webExchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //6.放行
        return chain.filter(exchange);
    }
    /**
     * 优先级设置
     * @return 返回设置优先级
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
