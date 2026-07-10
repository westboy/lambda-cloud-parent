package com.lambda.cloud.sse.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * SSE 响应头拦截器
 *
 * <p>为订阅响应注入防缓冲 / 禁缓存头，避免中间代理（如 nginx 默认 proxy_buffering）缓存心跳帧，
 * 导致客户端长时间收不到数据而被空闲断开。Connection 由容器自行管理，此处不覆盖。
 *
 * <p>通过 WebMvcConfigurer 仅注册到订阅路径，因此 preHandle 无需再次判断路径。
 *
 * @author Jin
 */
public class SseResponseHeadersInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        return true;
    }
}
