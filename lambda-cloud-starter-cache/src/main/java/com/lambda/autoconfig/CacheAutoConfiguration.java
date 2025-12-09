package com.lambda.autoconfig;

import com.lambda.cloud.cache.CacheManager;
import com.lambda.cloud.cache.provider.CaffeineCacheManager;
import com.lambda.cloud.cache.provider.MultiLevelCacheManager;
import com.lambda.cloud.cache.provider.RedisCacheManager;
import com.lambda.cloud.cache.support.CacheMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 缓存自动配置
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(prefix = "lambda.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheAutoConfiguration {

    /**
     * Caffeine缓存配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.github.benmanes.caffeine.cache.Caffeine")
    @ConditionalOnProperty(prefix = "lambda.cache", name = "type", havingValue = "CAFFEINE")
    static class CaffeineCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CacheManager cacheManager(CacheProperties properties) {
            log.info("Initializing Caffeine cache manager");
            return new CaffeineCacheManager();
        }
    }

    /**
     * Redis缓存配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnProperty(prefix = "lambda.cache", name = "type", havingValue = "REDIS", matchIfMissing = true)
    static class RedisCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate, CacheProperties properties) {
            log.info("Initializing Redis cache manager");
            return new RedisCacheManager(redisTemplate);
        }
    }

    /**
     * 多级缓存配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = { "com.github.benmanes.caffeine.cache.Caffeine",
            "org.springframework.data.redis.core.RedisTemplate" })
    @ConditionalOnProperty(prefix = "lambda.cache", name = "type", havingValue = "MULTI_LEVEL")
    static class MultiLevelCacheConfiguration {

        @Bean
        public String cacheNodeId() {
            return java.util.UUID.randomUUID().toString();
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheManager cacheManager(
                RedisTemplate<Object, Object> redisTemplate,
                CacheProperties properties,
                String cacheNodeId) {
            log.info("Initializing multi-level cache manager (L1: Caffeine, L2: Redis), NodeID: {}", cacheNodeId);
            return new MultiLevelCacheManager(redisTemplate, cacheNodeId);
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheMessageListener cacheMessageListener(
                CacheManager cacheManager,
                RedisTemplate<Object, Object> redisTemplate,
                String cacheNodeId) {
            return new CacheMessageListener(cacheManager, redisTemplate, cacheNodeId);
        }

        @Bean
        public RedisMessageListenerContainer redisMessageListenerContainer(
                RedisConnectionFactory connectionFactory,
                CacheMessageListener listener) {
           RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener(listener,
                    new ChannelTopic("lambda:cache:topic"));
            return container;
        }
    }
}
