package com.lambda.cloud.datasource.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.strategy.LoadBalanceDynamicDataSourceStrategy;
import com.lambda.cloud.datasource.condition.StandardDataSourceCondition;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.*;
import org.springframework.util.StringUtils;

/**
 * @author w
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@Conditional(StandardDataSourceCondition.class)
@Import(DataSourceAutoConfiguration.class)
public class StandardDataSourceConfigurer {

    public static final String PRIMARY_DATASOURCE = "primary";

    public StandardDataSourceConfigurer() {
        log.trace("SingleDataSourceConfigurer initializing...");
    }

    @RefreshScope
    @Bean(destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource hikariDataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        if (StringUtils.hasText(properties.getName())) {
            dataSource.setPoolName(properties.getName());
        }
        return dataSource;
    }

    @Bean
    @Primary
    public DynamicRoutingDataSource dynamicRoutingDataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
        DynamicRoutingDataSource dataSource =
                new DynamicRoutingDataSource(Collections.singletonList(dynamicDataSourceProvider));
        dataSource.setPrimary(PRIMARY_DATASOURCE);
        dataSource.setStrategy(LoadBalanceDynamicDataSourceStrategy.class);
        dataSource.setStrict(false);
        dataSource.setP6spy(false);
        dataSource.setSeata(false);
        return dataSource;
    }

    @Bean
    public DynamicDataSourceProvider dynamicDataSourceProvider(HikariDataSource hikariDataSource) {
        return () -> Collections.singletonMap(PRIMARY_DATASOURCE, hikariDataSource);
    }
}
