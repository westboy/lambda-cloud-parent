package com.lambda.autoconfig;

import com.lambda.cloud.mybatis.purview.config.PurviewPropertiesHolder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 数据权限自动配置
 *
 * @author Jin
 */
@AutoConfiguration
@EnableConfigurationProperties(PurviewProperties.class)
public class PurviewAutoConfiguration {

    @Bean
    public PurviewConfigInitializer purviewConfigInitializer(PurviewProperties properties) {
        return new PurviewConfigInitializer(properties);
    }

    public static class PurviewConfigInitializer {
        public PurviewConfigInitializer(PurviewProperties properties) {
            PurviewPropertiesHolder.initialize(properties);
        }
    }
}
