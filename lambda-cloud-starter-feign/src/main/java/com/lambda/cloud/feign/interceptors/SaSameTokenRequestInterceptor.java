package com.lambda.cloud.feign.interceptors;

import cn.dev33.satoken.same.SaSameUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.PriorityOrdered;

/**
 * Feign 出站同源令牌拦截器：为服务间 Feign 调用注入 Sa-Token 同源令牌头
 * （{@code SaSameUtil.SAME_TOKEN}），与网关 {@code ForwardAuthFilter} 对等，
 * 供下游服务 {@code SaServletFilter} 的同源校验放行，避免内部服务间调用被当作越权请求拦截。
 *
 * <p>无论本服务是否开启同源校验，出站调用都应携带同源令牌：下游是否校验由下游决定。
 * 令牌解析异常时仅告警并跳过，不中断 Feign 调用本身。
 *
 * @author Jin
 */
@Slf4j
public class SaSameTokenRequestInterceptor implements RequestInterceptor, PriorityOrdered {

    @Override
    public void apply(RequestTemplate template) {
        try {
            String sameToken = SaSameUtil.getToken();
            if (sameToken != null && !sameToken.isEmpty()) {
                template.header(SaSameUtil.SAME_TOKEN, sameToken);
            }
        } catch (Exception e) {
            log.warn("unable to resolve sa-token same-token, skip injecting header: {}", e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
