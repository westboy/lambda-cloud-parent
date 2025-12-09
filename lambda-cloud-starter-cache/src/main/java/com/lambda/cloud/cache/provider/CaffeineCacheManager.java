package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.Cache;
import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheType;
import com.lambda.cloud.cache.support.AbstractCacheManager;

/**
 * Caffeine缓存管理器
 */
public class CaffeineCacheManager extends AbstractCacheManager {

    public CaffeineCacheManager() {
        super(CacheType.CAFFEINE);
    }

    @Override
    protected <K, V> Cache<K, V> createCache(String name, CacheConfig config) {
        return new CaffeineCache<>(name, config);
    }
}
