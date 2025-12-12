package com.lambda.autoconfig;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.lambda.cloud.cache.CacheConstants;
import com.lambda.cloud.cache.provider.MultiLevelCacheManager;
import com.lambda.cloud.cache.support.CacheMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 缓存自动配置
 * <p>
 * 支持类型:
 * <ul>
 * <li>MULTI_LEVEL - 多级缓存 (L1: Caffeine, L2: Redis)</li>
 * </ul>
 */
@Slf4j
@AutoConfiguration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(prefix = "lambda.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheAutoConfiguration {

    /**
     * Caffeine 缓存配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Caffeine.class)
    @ConditionalOnProperty(prefix = "lambda.cache", name = "type", havingValue = "CAFFEINE")
    static class CaffeineCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager cacheManager(CacheProperties properties) {
            log.info("Initializing Caffeine cache manager");
            CaffeineCacheManager cacheManager = new CaffeineCacheManager();
            Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder();

            if (properties.getDefaults().getTtl() != null) {
                caffeineBuilder.expireAfterWrite(properties.getDefaults().getTtl());
            }
            if (properties.getDefaults().getMaxSize() > 0) {
                caffeineBuilder.maximumSize(properties.getDefaults().getMaxSize());
            }
            if (properties.getDefaults().getInitialCapacity() > 0) {
                caffeineBuilder.initialCapacity(properties.getDefaults().getInitialCapacity());
            }
            if (properties.getDefaults().isEnableStats()) {
                caffeineBuilder.recordStats();
            }

            cacheManager.setCaffeine(caffeineBuilder);
            return cacheManager;
        }
    }

    /**
     * Redis 缓存配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnProperty(prefix = "lambda.cache", name = "type", havingValue = "REDIS", matchIfMissing = true)
    static class RedisCacheConfigurationConfig {

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory, CacheProperties properties) {
            log.info("Initializing Redis cache manager");

            RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
            if (properties.getDefaults().getTtl() != null) {
                defaultCacheConfig = defaultCacheConfig.entryTtl(properties.getDefaults().getTtl());
            }
            if (properties.getDefaults().getKeyPrefix() != null) {
                defaultCacheConfig = defaultCacheConfig.prefixCacheNameWith(
                        properties.getDefaults().getKeyPrefix());
            }
            if (!properties.getDefaults().isAllowNullValues()) {
                defaultCacheConfig = defaultCacheConfig.disableCachingNullValues();
            }

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultCacheConfig)
                    .build();
        }
    }

    /**
     * 多级缓存配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ Caffeine.class, RedisTemplate.class })
    @ConditionalOnProperty(prefix = "lambda.cache", name = "type", havingValue = "MULTI_LEVEL")
    static class MultiLevelCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "cacheNodeId")
        public String cacheNodeId() {
            return java.util.UUID.randomUUID().toString();
        }

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager cacheManager(
                RedisTemplate<Object, Object> redisTemplate, CacheProperties properties, String cacheNodeId) {
            log.info("Initializing multi-level cache manager (L1: Caffeine, L2: Redis), NodeID: {}", cacheNodeId);
            return new MultiLevelCacheManager(redisTemplate, properties, cacheNodeId);
        }

        @Bean
        @ConditionalOnMissingBean(CacheMessageListener.class)
        public CacheMessageListener cacheMessageListener(
                CacheManager cacheManager, RedisTemplate<Object, Object> redisTemplate, String cacheNodeId) {
            return new CacheMessageListener(cacheManager, redisTemplate, cacheNodeId);
        }

        @Bean
        @ConditionalOnMissingBean(RedisMessageListenerContainer.class)
        public RedisMessageListenerContainer redisMessageListenerContainer(
                RedisConnectionFactory connectionFactory, CacheMessageListener listener) {
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener(listener, new ChannelTopic(CacheConstants.CACHE_SYNC_TOPIC));
            return container;
        }
    }
}
