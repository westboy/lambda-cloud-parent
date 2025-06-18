package com.lambda.autoconfig;

import com.lambda.cloud.sse.SseEmitterManager;
import com.lambda.cloud.sse.controller.SseController;
import com.lambda.cloud.sse.SseEventListener;
import com.lambda.cloud.sse.listener.DefaultSseEventListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * SseAutoConfiguration
 *
 * @author Jin
 */
@AutoConfiguration
@EnableConfigurationProperties(SseProperties.class)
public class SseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SseEmitterManager sseEmitterManager(SseProperties properties) {
        return new SseEmitterManager(properties);
    }

    @Bean
    @ConditionalOnProperty(name = "lambda.sse.enable-controller", havingValue = "true", matchIfMissing = true)
    public SseController sseController(SseEmitterManager emitterManager) {
        return new SseController(emitterManager);
    }

    @Bean
    @ConditionalOnProperty(name = "lambda.sse.enable-logging-listener", havingValue = "true", matchIfMissing = true)
    public SseEventListener loggingSseEventListener() {
        return new DefaultSseEventListener();
    }
}
