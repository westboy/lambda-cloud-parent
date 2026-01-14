package com.lambda.autoconfig;

import cn.hutool.core.util.IdUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheConstants;
import com.lambda.cloud.cache.provider.MultiLevelCacheManager;
import com.lambda.cloud.cache.support.CacheMessageListener;
import com.lambda.cloud.cache.support.CaffeineFactory;
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
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Map;

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
            
            // 使用默认配置构建Caffeine
            CacheConfig defaultConfig = properties.getDefaults().toCacheConfig("default");
            Caffeine<Object, Object> defaultBuilder = CaffeineFactory.createCaffeine(defaultConfig);
            
            cacheManager.setCaffeine(defaultBuilder);
            cacheManager.setAllowNullValues(properties.getDefaults().isAllowNullValues());

            // 注册自定义配置的缓存
            properties.getCaches().forEach((name, configProperties) -> {
                CacheConfig config = configProperties.toCacheConfig(name);
                Caffeine<Object, Object> builder = CaffeineFactory.createCaffeine(config);
                cacheManager.registerCustomCache(name, builder.build());
            });

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

            // 构造默认配置
            RedisCacheConfiguration defaultCacheConfig = createRedisCacheConfiguration(properties.getDefaults());

            // 构造多缓存配置
            Map<String, RedisCacheConfiguration> initialCacheConfigurations = new java.util.HashMap<>();
            properties.getCaches().forEach((name, config) -> {
                initialCacheConfigurations.put(name, createRedisCacheConfiguration(config));
            });

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultCacheConfig)
                    .withInitialCacheConfigurations(initialCacheConfigurations)
                    .build();
        }

        private RedisCacheConfiguration createRedisCacheConfiguration(CacheProperties.CacheConfigProperties properties) {
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
            
            // 统一序列化策略：Key 使用 String，Value 使用 GenericJackson2Json
            config = config.serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                            RedisSerializer.string()))
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(
                                    RedisSerializer.json()));

            if (properties.getTtl() != null) {
                config = config.entryTtl(properties.getTtl());
            }
            if (properties.getKeyPrefix() != null) {
                config = config.prefixCacheNameWith(properties.getKeyPrefix());
            }
            if (!properties.isAllowNullValues()) {
                config = config.disableCachingNullValues();
            }
            return config;
        }
    }

    /**
     * 多级缓存配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({Caffeine.class, RedisTemplate.class})
    @ConditionalOnProperty(prefix = "lambda.cache", name = "type", havingValue = "MULTI_LEVEL")
    static class MultiLevelCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "cacheNodeId")
        public String cacheNodeId() {
            return IdUtil.fastSimpleUUID();
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
