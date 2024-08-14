package com.jingfang.autoconfig;

import com.jingfang.cloud.logger.advices.OperationLoggerAdvice;
import com.jingfang.cloud.logger.service.DefaultOperationServiceImpl;
import com.jingfang.cloud.logger.service.OperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggerAutoConfiguration {
    public LoggerAutoConfiguration() {
        log.trace("initializing...");
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class ServletConfiguration {
        @Bean
        public OperationLoggerAdvice operationLoggerAdvice(OperationService operationService) {
            return new OperationLoggerAdvice(operationService);
        }
    }


    @Bean
    @ConditionalOnMissingBean
    public OperationService defaultOperationService() {
        return new DefaultOperationServiceImpl();
    }

}
