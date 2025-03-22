package com.lamuda.cloud.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * WebSocketExpandFilter
 *
 * @author jpjoo
 */
@Slf4j
public class WebSocketExpandFilter implements GlobalFilter, Ordered {
    private static final String WS = "ws";
    private static final String WSS = "wss";

    private final String path;

    public WebSocketExpandFilter(String path) {
        this.path = path;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
        String scheme = uri.getScheme();
        if (check(uri, scheme)) {
            String changed = convertScheme(scheme);
            uri = UriComponentsBuilder.fromUri(uri).scheme(changed).build().toUri();
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, uri);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 2;
    }


    private boolean check(URI uri, String scheme) {
        boolean c1 = WS.equalsIgnoreCase(scheme) || WSS.equalsIgnoreCase(scheme);
        boolean c2 = path.equals(uri.getPath());
        return c1 && c2;
    }

    private String convertScheme(String scheme) {
        return WS.equalsIgnoreCase(scheme) ? "http" : "https";
    }
}