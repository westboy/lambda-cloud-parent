package com.lambda.autoconfig;

import com.lambda.cloud.sse.SseEmitterManager;
import com.lambda.cloud.sse.cluster.ClusterSseEmitterManager;
import com.lambda.cloud.sse.controller.SseController;
import com.lambda.cloud.sse.initializer.SseEmitterInitializer;
import com.lambda.cloud.sse.interceptor.SseResponseHeadersInterceptor;
import com.lambda.cloud.sse.service.SseService;
import com.lambda.cloud.sse.service.SseServiceImpl;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
    @ConditionalOnProperty(name = "lambda.sse.enable-endpoint", havingValue = "true", matchIfMissing = true)
    public SseController sseController(SseService sseService) {
        return new SseController(sseService);
    }

    @Bean
    @ConditionalOnProperty(name = "lambda.sse.enable-endpoint", havingValue = "true", matchIfMissing = true)
    public WebMvcConfigurer sseWebMvcConfigurer(SseProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                String pattern = properties.getEndpointPrefix() + properties.getSubscribePath() + "/**";
                registry.addInterceptor(new SseResponseHeadersInterceptor()).addPathPatterns(pattern);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public SseService sseService(
            SseEmitterManager emitterManager,
            @Autowired(required = false) SseEmitterInitializer sseEmitterInitializer) {
        return new SseServiceImpl(emitterManager, sseEmitterInitializer);
    }
}
