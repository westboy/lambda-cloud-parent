package com.jingfang.autoconfig;

import com.jingfang.cloud.logger.LoggingExtendProperties;
import com.jingfang.cloud.logger.advices.OperationLoggerAdvice;
import com.jingfang.cloud.logger.service.DefaultOperationServiceImpl;
import com.jingfang.cloud.logger.service.OperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LoggerAutoConfiguration
 *
 * @author jpjoo
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(KafkaAutoConfiguration.class)
@EnableConfigurationProperties(LoggingExtendProperties.class)
public class LoggerAutoConfiguration {
    public LoggerAutoConfiguration() {
        log.trace("initializing...");
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class ServletConfiguration {
        @Bean
        public OperationLoggerAdvice operationLoggerAdvice() {
            return new OperationLoggerAdvice();
        }
    }


    @Bean
    @ConditionalOnMissingBean
    public OperationService defaultOperationService() {
        return new DefaultOperationServiceImpl();
    }

}
