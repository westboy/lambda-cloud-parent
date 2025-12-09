package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.Cache;
import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheType;
import com.lambda.cloud.cache.support.AbstractCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis缓存管理器
 */
@Slf4j
public class RedisCacheManager extends AbstractCacheManager {

    private final RedisTemplate<Object, Object> redisTemplate;

    public RedisCacheManager(RedisTemplate<Object, Object> redisTemplate) {
        super(CacheType.REDIS);
        this.redisTemplate = redisTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K, V> Cache<K, V> createCache(String name, CacheConfig config) {
        return new RedisCache<>(name, (RedisTemplate<K, V>) redisTemplate, config);
    }
}
