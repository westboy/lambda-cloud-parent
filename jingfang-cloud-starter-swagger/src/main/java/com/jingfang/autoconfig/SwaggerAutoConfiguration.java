package com.jingfang.autoconfig;


import com.jingfang.cloud.swagger.converter.PageConverter;
import com.jingfang.cloud.swagger.filter.SwaggerDisabledFilter;
import com.jingfang.cloud.swagger.SwaggerProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
        return new OpenAPI().info(new Info().title(info.getTitle()).version(info.getVersion()));
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
    public PageConverter pageconverter() {
        return new PageConverter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "jingfang.api-docs", name = "enabled", havingValue = "false")
    public SwaggerDisabledFilter swaggerDisabledFilter(SwaggerProperties properties) {
        return new SwaggerDisabledFilter(properties.getDocUri());
    }

}
