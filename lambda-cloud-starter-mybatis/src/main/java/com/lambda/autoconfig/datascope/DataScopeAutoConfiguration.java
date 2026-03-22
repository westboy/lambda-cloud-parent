package com.lambda.autoconfig.datascope;

import com.lambda.cloud.mybatis.datascope.DataScopePropertiesHolder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 数据权限自动配置
 *
 * @author Jin
 */
@AutoConfiguration
@EnableConfigurationProperties(DataScopeProperties.class)
public class DataScopeAutoConfiguration {

    @Bean
    public ApplicationRunner dataScopeConfigInitializer(DataScopeProperties properties) {
        return args -> DataScopePropertiesHolder.initialize(properties);
    }
}
