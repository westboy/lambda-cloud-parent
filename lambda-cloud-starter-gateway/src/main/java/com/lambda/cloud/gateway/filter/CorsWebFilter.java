package com.lambda.cloud.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsProcessor;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * CorsWebFilter
 *
 * @author jpjoo
 */
public record CorsWebFilter(CorsConfigurationSource configSource, CorsProcessor processor) implements WebFilter {

    public CorsWebFilter(CorsConfigurationSource configSource) {
        this(configSource, new DefaultCorsProcessor());
    }

    public CorsWebFilter {
        Assert.notNull(configSource, "CorsConfigurationSource must not be null");
        Assert.notNull(processor, "CorsProcessor must not be null");
    }

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        CorsConfiguration corsConfiguration = this.configSource.getCorsConfiguration(exchange);

        boolean isValid = this.processor.process(corsConfiguration, exchange);
        if (!isValid || CorsUtils.isPreFlightRequest(request)) {
            return Mono.empty();
        }

        return chain.filter(exchange);
    }
}
