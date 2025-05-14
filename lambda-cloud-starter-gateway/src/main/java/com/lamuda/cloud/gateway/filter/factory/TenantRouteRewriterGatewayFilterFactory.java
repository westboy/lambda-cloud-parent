package com.lambda.cloud.gateway.filter.factory;

import com.google.common.collect.Lists;
import com.lambda.cloud.gateway.service.TenantRouteService;
import com.lambda.cloud.gateway.utils.WebFluxUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

/**
 * TenantRouteRewriterGatewayFilterFactory
 *
 * @author jpjoo
 */
@Slf4j
public final class TenantRouteRewriterGatewayFilterFactory extends AbstractGatewayFilterFactory<TenantRouteRewriterGatewayFilterFactory.Config> {

    private final TenantRouteService service;

    public TenantRouteRewriterGatewayFilterFactory(TenantRouteService service) {
        super(Config.class);
        this.service = service;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Lists.newArrayList("key");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String tenant = getTenantId(request, config.getKey());
            log.debug("tenant: {}", tenant);
            if (StringUtils.isNotBlank(tenant) && service.verify(tenant)) {
                URI uri = service.getUri(tenant, request);
                if (uri != null) {
                    WebFluxUtils.rewriteExchangeUrl(exchange, uri);
                }
            }
            return chain.filter(exchange);
        };
    }

    /***
     *
     * @param request
     * @param key
     * @return java.lang.String
     */
    private String getTenantId(ServerHttpRequest request, String key) {
        String tenantid = request.getHeaders().getFirst(key);
        if (StringUtils.isBlank(tenantid)) {
            tenantid = request.getQueryParams().getFirst(key);
        }
        return tenantid;
    }


    @Getter
    @Validated
    public static class Config {
        private String key = "tenantId";

        public Config setKey(String key) {
            if (StringUtils.isNotBlank(key)) {
                this.key = key;
            }
            return this;
        }
    }
}
