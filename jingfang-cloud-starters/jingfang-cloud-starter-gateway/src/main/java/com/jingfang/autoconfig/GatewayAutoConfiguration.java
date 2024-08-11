package com.jingfang.autoconfig;


import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.jingfang.cloud.core.propertis.CorsProperties;
import com.jingfang.cloud.gateway.filter.*;
import com.jingfang.cloud.gateway.predicate.BackendRoutePredicateFactory;
import com.jingfang.cloud.gateway.properties.GatewayAccessProperties;
import com.jingfang.cloud.gateway.service.RouterEnhancer;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * GatewayAutoConfiguration
 *
 * @author jpjoo
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(org.springframework.cloud.gateway.config.GatewayAutoConfiguration.class)
@EnableConfigurationProperties(GatewayAccessProperties.class)
public class GatewayAutoConfiguration {

    private static final String INDEX = "/index.html";

    @Bean
    public WebSocketClient webSocketClient() {
        WebsocketClientSpec.Builder builder = WebsocketClientSpec.builder();
        builder.maxFramePayloadLength(524288);
        return new ReactorNettyWebSocketClient(HttpClient.create(), builder);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "jingfang.api-docs.production", havingValue = "true")
    public static class SwaggerDisabledConfigurer {
        @Bean
        public RouterFunction<ServerResponse> routerFunction() {
            RequestPredicate predicate = GET("/doc.html").or(GET("/v3/api-docs/**"))
                    .or(GET("/swagger-resources/**"));
            return route(predicate, r -> ServerResponse.status(HttpStatus.NOT_FOUND).build());
        }
    }


    @Bean
    public XFrameOptionsFilter xframeOptionsFilter() {
        return new XFrameOptionsFilter();
    }

    @Bean
    public ForwardAuthFilter forwardAuthFilter() {
        return new ForwardAuthFilter();
    }

    @Bean
    public GlobalCacheRequestFilter globalCacheRequestFilter() {
        return new GlobalCacheRequestFilter();
    }

    @Bean
    public WebSocketExpandFilter webSocketExpandFilter() {
        return new WebSocketExpandFilter("/ws/info");
    }

    @Bean
    public RouterFunction<ServerResponse> indexRouter() throws URISyntaxException {
        URI index = new URI(INDEX);
        return route(GET("/"), _ -> ServerResponse.temporaryRedirect(index).build());
    }

    @Bean
    @ConfigurationProperties(prefix = "jingfang.web.cors")
    public CorsProperties corsProperties() {
        return new CorsProperties();
    }

    /**
     * 注册 Sa-Token 全局过滤器
     */
    @Bean
    public SaReactorFilter getSaReactorFilter(GatewayAccessProperties gatewayAccessProperties) {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude("/favicon.ico", "/actuator/**")
                .setAuth(_ -> {
                    SaRouter.match("/**")
                            .notMatch(gatewayAccessProperties.getWhites())
                            .check(_ -> StpUtil.checkLogin());
                }).setError(_ -> SaResult.error("认证失败，无法访问系统资源").setCode(HttpStatus.UNAUTHORIZED.value()));
    }

    @Bean
    @ConditionalOnProperty(prefix = "jingfang.web.cors", name = "enable", havingValue = "true")
    public CorsWebFilter corsWebFilter(CorsProperties corsProperties) {
        CorsConfiguration corsConfig = new CorsConfiguration();
        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (CollectionUtils.isNotEmpty(allowedOrigins)) {
            corsConfig.setAllowedOrigins(allowedOrigins);
        } else {
            corsConfig.setAllowedOrigins(Collections.singletonList(CorsProperties.ALL_PATH));
        }
        corsConfig.setMaxAge(corsProperties.getMaxAge());
        CorsProperties.ALLOWED_METHOD.forEach(method -> corsConfig.addAllowedMethod(HttpMethod.valueOf(method)));
        CorsProperties.ALLOWED_HEADERS.forEach(corsConfig::addAllowedHeader);
        CorsProperties.EXPOSED_HEADERS.forEach(corsConfig::addExposedHeader);
        corsConfig.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(CorsProperties.ALL_PATH, corsConfig);
        return new CorsWebFilter(source);
    }


    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder locatorBuilder, List<RouterEnhancer> routes) {
        RouteLocatorBuilder.Builder builder = locatorBuilder.routes();
        if (CollectionUtils.isNotEmpty(routes)) {
            routes.forEach(router -> router.config(builder));
        }
        return builder.build();
    }

    @Bean
    public BackendRoutePredicateFactory backend() {
        return new BackendRoutePredicateFactory();
    }

    @Bean
    public NettyServerCustomizer nettyServerCustomizer(HttpClientProperties properties) {
        final DataSize maxInitialLineLength = properties.getMaxInitialLineLength();
        final Duration idleTimeout = Optional.ofNullable(properties.getPool().getMaxIdleTime()).orElse(Duration.ofSeconds(10));
        return httpServer -> {
            httpServer = httpServer.idleTimeout(idleTimeout);
            if (maxInitialLineLength != null) {
                httpServer = httpServer.httpRequestDecoder(options -> options.maxInitialLineLength((int) maxInitialLineLength.toBytes()));
            }
            return httpServer;
        };
    }
}
