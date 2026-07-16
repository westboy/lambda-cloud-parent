package com.lambda.cloud.cache.provider;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.lambda.autoconfig.CacheProperties;
import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheConstants;
import com.lambda.cloud.cache.support.CaffeineFactory;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 多级缓存管理器
 * <p>
 * L1: Caffeine
 * L2: Redis
 */
@Slf4j
public class MultiLevelCacheManager extends AbstractCacheManager {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final CacheProperties properties;
    private final String nodeId;
    private RedisCacheWriter cacheWriter;

    @PostConstruct
    public void init() {
        var connectionFactory = redisTemplate.getConnectionFactory();
        if (connectionFactory == null) {
            throw new IllegalStateException("RedisTemplate must have RedisConnectionFactory");
        }
        // 复用 writer
        cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
    }

    public MultiLevelCacheManager(
            RedisTemplate<Object, Object> redisTemplate, CacheProperties properties, String nodeId) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.nodeId = nodeId;
    }

    @Override
    protected @NonNull Collection<? extends Cache> loadCaches() {
        return Collections.emptyList();
    }

    @Override
    protected Cache getMissingCache(@NonNull String name) {
        CacheConfig config = properties.getCacheConfig(name);

        // 创建 L2 (Redis) - 必须先创建L2，因为L1的CacheLoader可能需要依赖L2
        Cache l2Cache = createRedisCache(name, config);

        // 创建 L1 (Caffeine)
        Cache l1Cache = createCaffeineCache(name, config, l2Cache);

        return new MultiLevelCache(name, l1Cache, l2Cache, redisTemplate, CacheConstants.CACHE_SYNC_TOPIC, nodeId);
    }

    private Cache createCaffeineCache(String name, CacheConfig config, Cache l2Cache) {
        Caffeine<Object, Object> builder = CaffeineFactory.createCaffeine(config);

        // 如果配置了 refreshAfterWrite，则必须提供 CacheLoader
        // 这里使用 L2CacheLoader 从 Redis 中加载数据
        if (config.getRefreshAfterWrite() != null) {
            com.github.benmanes.caffeine.cache.CacheLoader<Object, Object> loader = key -> {
                try {
                    Cache.ValueWrapper wrapper = l2Cache.get(key);
                    return wrapper != null ? wrapper.get() : null;
                } catch (Exception e) {
                    log.warn("Failed to load key from L2 cache: {}", key, e);
                    return null;
                }
            };
            return new CaffeineCache(name, builder.build(loader), config.isAllowNullValues());
        }

        return new CaffeineCache(name, builder.build(), config.isAllowNullValues());
    }

    private Cache createRedisCache(String name, CacheConfig config) {

        RedisCacheConfiguration redisConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(config.getTtl() != null ? config.getTtl() : Duration.ofHours(1));

        if (!config.isAllowNullValues()) {
            redisConfig = redisConfig.disableCachingNullValues();
        }

        // 强制 key 使用 string 序列化（避免 Redis CLI 中 key 不可读）
        redisConfig = redisConfig.serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));

        // value 序列化继承自 redisTemplate
        redisConfig = redisConfig.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()));

        // 全局前缀 + cacheName
        String globalPrefix = config.getKeyPrefix();
        redisConfig = redisConfig.computePrefixWith(cacheName -> (globalPrefix == null || globalPrefix.isEmpty()
                        ? ""
                        : globalPrefix.endsWith(":") ? globalPrefix : globalPrefix + ":")
                + cacheName
                + ":");

        return new PublicRedisCache(name, cacheWriter, redisConfig);
    }

    private static class PublicRedisCache extends RedisCache {
        public PublicRedisCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
            super(name, cacheWriter, cacheConfig);
        }
    }
}
