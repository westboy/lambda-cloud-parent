package com.lambda.autoconfig;

import com.lambda.cloud.swagger.filter.SwaggerDisabledFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author w
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerAutoConfiguration {

    @Value("${spring.application.name:unknown}")
    private String application;

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openApi(SwaggerProperties info) {
        OpenAPI openAPI = new OpenAPI()
                .openapi(info.getOpenApiVersion().getVersion())
                .info(new Info().title(info.getTitle()).version(info.getVersion()));
        if (Boolean.TRUE.equals(info.getTokenEnabled())) {
            String schemeName = info.getTokenSchemeName();
            openAPI.components(new Components()
                    .addSecuritySchemes(
                            schemeName,
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .name(info.getTokenName())
                                    .in(SecurityScheme.In.HEADER)
                                    .scheme(info.getTokenScheme())
                                    .bearerFormat(info.getTokenBearerFormat())
                                    .description(info.getTokenDescription())));
            openAPI.addSecurityItem(new SecurityRequirement().addList(schemeName));
        }
        return openAPI;
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupedOpenApi defaultGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group(application)
                .pathsToMatch("/**")
                .addOpenApiMethodFilter(method -> method.isAnnotationPresent(Operation.class))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "lambda.api-docs", name = "enabled", havingValue = "false")
    public SwaggerDisabledFilter swaggerDisabledFilter(SwaggerProperties properties) {
        return new SwaggerDisabledFilter(properties.getDocUri());
    }
}
