package com.lambda.autoconfig;

import com.lambda.cloud.sse.SseEmitterManager;
import com.lambda.cloud.sse.SseEventListener;
import com.lambda.cloud.sse.cluster.ClusterSseEmitterManager;
import com.lambda.cloud.sse.controller.SseController;
import com.lambda.cloud.sse.listener.DefaultSseEventListener;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SSE自动配置
 *
 * @author Jin
 */
@Configuration
@EnableConfigurationProperties(SseProperties.class)
public class SseAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "lambda.sse.cluster.enabled", havingValue = "false", matchIfMissing = true)
    public SseEmitterManager localSseEmitterManager(SseProperties properties) {
        return new SseEmitterManager(properties);
    }

    @Bean
    @ConditionalOnProperty(name = "lambda.sse.cluster.enabled", havingValue = "true")
    public SseEmitterManager distributedSseEmitterManager(SseProperties properties, RedissonClient redissonClient) {
        return new ClusterSseEmitterManager(properties, redissonClient);
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
