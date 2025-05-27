package com.lambda.cloud.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * XFrameOptionsFilter
 *
 * @author jpjoo
 */
@Slf4j
public class XFrameOptionsFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .then(Mono.just(exchange))
                .map(serverWebExchange -> {
                    HttpHeaders headers = serverWebExchange.getResponse().getHeaders();
                    headers.addIfAbsent("X-Frame-Options", "SAMEORIGIN");
                    return serverWebExchange;
                })
                .then();
    }
}
