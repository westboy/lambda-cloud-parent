package com.lambda.cloud.gateway.swagger;

import io.swagger.v3.oas.annotations.Hidden;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * SwaggerResourceController
 *
 * @author jpjoo
 */
@RestController
public class SwaggerResourceController {

    private static final String API_NAME = "api-name";
    private static final String API_URI = "api-docs";
    private static final String API_ORDER = "api-order";
    private final GatewayProperties gatewayProperties;

    public SwaggerResourceController(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Value("${springdoc.api-docs.path:/v3/api-docs}/swagger-config")
    private String configUrl;


    @Hidden
    @GetMapping(
            value = {"${springdoc.api-docs.path:/v3/api-docs}/swagger-config"},
            produces = {"application/json"}
    )
    public ConfigResource getSwaggerUiConfig(ServerHttpRequest request) {
        ConfigResource config = new ConfigResource();
        config.setConfigUrl(configUrl);
        config.setValidatorUrl(EMPTY);
        config.setOauth2RedirectUrl(EMPTY);
        List<ConfigResource.Group> groups = gatewayProperties.getRoutes().stream()
                .filter(this::isDocumentSupported)
                .sorted(Comparator.comparing(this::getOrder))
                .map(this::buildSwaggerResource).collect(Collectors.toList());
        config.setGroups(groups);
        return config;
    }


    /**
     * 是否包含有API文档
     *
     * @param definition
     * @return
     */
    private boolean isDocumentSupported(@NonNull RouteDefinition definition) {
        return definition.getMetadata().containsKey(API_URI);
    }

    /**
     * 生成API定义
     *
     * @param definition
     * @return springfox.documentation.swagger.web.SwaggerResource
     */
    private ConfigResource.Group buildSwaggerResource(@NonNull RouteDefinition definition) {
        Map<String, Object> metadata = definition.getMetadata();
        String url = metadata.get(API_URI).toString();
        String name = MapUtils.getString(metadata, API_NAME, definition.getId());
        return new ConfigResource.Group(name, url);
    }

    /**
     * 获取文档排序
     *
     * @param definition
     * @return int
     */
    private int getOrder(@NonNull RouteDefinition definition) {
        Object order = definition.getMetadata().get(API_ORDER);
        if (order instanceof Integer) {
            return (int) order;
        }
        return Integer.MAX_VALUE;
    }
}
