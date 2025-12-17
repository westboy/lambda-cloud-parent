package com.lambda.autoconfig;

import com.lambda.cloud.mybatis.purview.config.PurviewConfig;
import com.lambda.cloud.mybatis.purview.config.PurviewConfigHolder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 数据权限自动配置
 *
 * @author Jin
 */
@AutoConfiguration
@EnableConfigurationProperties(PurviewConfig.class)
public class PurviewAutoConfiguration {

    @Bean
    public PurviewConfigInitializer purviewConfigInitializer(PurviewConfig properties) {
        return new PurviewConfigInitializer(properties);
    }

    public static class PurviewConfigInitializer {
        public PurviewConfigInitializer(PurviewConfig properties) {
            PurviewConfigHolder.setInstance(properties);
        }
    }
}
