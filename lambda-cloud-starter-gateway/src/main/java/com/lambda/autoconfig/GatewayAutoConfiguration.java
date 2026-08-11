package com.lambda.autoconfig;

import static cn.dev33.satoken.SaManager.log;
import static com.lambda.cloud.core.Constants.GSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.core.shared.CorsProperty;
import com.lambda.cloud.core.shared.KeyValue;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.cloud.gateway.filter.*;
import com.lambda.cloud.gateway.listener.DynamicRouteConfigChangedListener;
import com.lambda.cloud.gateway.predicate.BackendRoutePredicateFactory;
import com.lambda.cloud.gateway.properties.GatewayFirewallProperties;
import com.lambda.cloud.gateway.service.RouterEnhancer;
import com.lambda.cloud.gateway.swagger.SwaggerResourceController;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.reactor.netty.NettyServerCustomizer;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
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
import tools.jackson.databind.ObjectMapper;

/**
 * GatewayAutoConfiguration
 *
 * @author jpjoo
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(org.springframework.cloud.gateway.config.GatewayAutoConfiguration.class)
@EnableConfigurationProperties(GatewayFirewallProperties.class)
public class GatewayAutoConfiguration {

    private static final String INDEX = "/index.html";

    @Bean
    public WebSocketClient webSocketClient() {
        WebsocketClientSpec.Builder builder = WebsocketClientSpec.builder();
        builder.maxFramePayloadLength(524288);
        return new ReactorNettyWebSocketClient(HttpClient.create(), builder);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "lambda.api-docs.production", havingValue = "true")
    public static class SwaggerDisabledConfigurer {
        @Bean
        public RouterFunction<ServerResponse> routerFunction() {
            RequestPredicate predicate =
                    GET("/doc.html").or(GET("/v3/api-docs/**")).or(GET("/swagger-resources/**"));
            return route(
                    predicate, e -> ServerResponse.status(HttpStatus.NOT_FOUND).build());
        }
    }

    @Bean
    public XFrameOptionsFilter xframeOptionsFilter() {
        return new XFrameOptionsFilter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "lambda.security.sa-token", name = "check-same-token")
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
        return route(GET("/"), e -> ServerResponse.temporaryRedirect(index).build());
    }

    @Bean
    @ConfigurationProperties(prefix = "lambda.web.cors")
    public CorsProperty corsProperties() {
        return new CorsProperty();
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "lambda.security.sa-token")
    public SaTokenConfig saTokenConfig() {
        return new SaTokenConfig();
    }

    /**
     * 注册 Sa-Token 全局过滤器
     */
    @Bean
    @ConditionalOnProperty(prefix = "lambda.web.firewall", name = "enabled")
    public SaReactorFilter getSaReactorFilter(GatewayFirewallProperties gatewayFirewallProperties) {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude("/favicon.ico", "/actuator/**")
                .setAuth(e1 -> SaRouter.match("/**")
                        .notMatch(gatewayFirewallProperties.getWhites())
                        .check(saRouterStaff ->
                                StpLogicUtils.getActiveStpLogic().checkLogin()))
                .setError(e -> {
                    ErrorModel errorModel = new ErrorModel();
                    errorModel.setStatus(HttpStatus.UNAUTHORIZED.value());
                    errorModel.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                    if (e instanceof SaTokenException saTokenException) {
                        errorModel.setError(String.valueOf(saTokenException.getCode()));
                    }
                    errorModel.setTimestamp(System.currentTimeMillis());
                    errorModel.setMessage(e.getMessage());

                    return GSON.toJson(errorModel);
                });
    }

    @Bean
    @ConditionalOnProperty(prefix = "lambda.web.firewall", name = "enabled")
    public ApplicationRunner applicationRunner(GatewayFirewallProperties gatewayFirewallProperties) {
        return args -> {
            List<KeyValue> loginTypes = gatewayFirewallProperties.getLoginTypes();
            if (!loginTypes.isEmpty()) {
                Set<String> initializeLoginTypes =
                        loginTypes.stream().map(KeyValue::getCode).collect(Collectors.toSet());
                initializeLoginTypes.forEach(type -> {
                    StpLogic newStpLogic = new StpLogic(type);
                    SaManager.putStpLogic(newStpLogic);
                });
                StpLogicUtils.initializeLoginTypes(initializeLoginTypes);
                log.trace("init sa-token login types: {}", initializeLoginTypes);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "lambda.web.cors", name = "enabled", havingValue = "true")
    public CorsWebFilter corsWebFilter(CorsProperty corsProperty) {
        CorsConfiguration corsConfig = new CorsConfiguration();
        List<String> allowedOrigins = corsProperty.getAllowedOrigins();
        Assert.state(
                CollectionUtils.isNotEmpty(allowedOrigins),
                "allowedOrigins must not be empty when lambda.web.cors.enabled is true");
        corsConfig.setAllowedOrigins(allowedOrigins);
        corsConfig.setAllowCredentials(corsProperty.isAllowCredentials());
        corsConfig.validateAllowCredentials();
        corsConfig.setMaxAge(corsProperty.getMaxAge());
        CorsProperty.ALLOWED_METHOD.forEach(method -> corsConfig.addAllowedMethod(HttpMethod.valueOf(method)));
        CorsProperty.ALLOWED_HEADERS.forEach(corsConfig::addAllowedHeader);
        CorsProperty.EXPOSED_HEADERS.forEach(corsConfig::addExposedHeader);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(CorsProperty.ALL_PATH, corsConfig);
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
        BackendRoutePredicateFactory backendRoutePredicateFactory = new BackendRoutePredicateFactory();
        backendRoutePredicateFactory.initialize();
        return backendRoutePredicateFactory;
    }

    @Bean
    public NettyServerCustomizer nettyServerCustomizer(HttpClientProperties properties) {
        final DataSize maxInitialLineLength = properties.getMaxInitialLineLength();
        final Duration idleTimeout =
                Optional.ofNullable(properties.getPool().getMaxIdleTime()).orElse(Duration.ofSeconds(10));
        return httpServer -> {
            httpServer = httpServer.idleTimeout(idleTimeout);
            if (maxInitialLineLength != null) {
                httpServer = httpServer.httpRequestDecoder(
                        options -> options.maxInitialLineLength((int) maxInitialLineLength.toBytes()));
            }
            return httpServer;
        };
    }

    @Bean
    public SwaggerResourceController swaggerResourceController(GatewayProperties gatewayProperties) {
        return new SwaggerResourceController(gatewayProperties);
    }

    @Bean
    public DynamicRouteConfigChangedListener dynamicRouteConfigChangedListener(
            Environment environment,
            RouteDefinitionWriter routeDefinitionWriter,
            ApplicationEventPublisher applicationEventPublisher,
            ObjectMapper objectMapper) {
        return new DynamicRouteConfigChangedListener(
                environment, routeDefinitionWriter, applicationEventPublisher, objectMapper);
    }
}
