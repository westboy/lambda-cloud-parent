package com.lambda.cloud.cache.support;

import com.lambda.cloud.cache.Cache;
import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheManager;
import com.lambda.cloud.cache.CacheType;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存管理器抽象基类
 */
@Slf4j
public abstract class AbstractCacheManager implements CacheManager {

    protected final ConcurrentHashMap<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();
    protected final CacheType cacheType;

    protected AbstractCacheManager(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name) {
        return (Cache<K, V>) cacheMap.get(name);
    }

    @Override
    public <K, V> Cache<K, V> getOrCreateCache(String name) {
        return getOrCreateCache(name, CacheConfig.defaultConfig(name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config) {
        return (Cache<K, V>) cacheMap.computeIfAbsent(name, key -> {
            log.info("Creating cache: name={}, type={}", name, cacheType);
            return createCache(name, config);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }

    @Override
    public boolean destroyCache(String name) {
        Cache<?, ?> cache = cacheMap.remove(name);
        if (cache != null) {
            cache.clear();
            log.info("Cache destroyed: name={}", name);
            return true;
        }
        return false;
    }

    @Override
    public void destroyAll() {
        cacheMap.values().forEach(Cache::clear);
        cacheMap.clear();
        log.info("All caches destroyed");
    }

    @Override
    public CacheType getCacheType() {
        return cacheType;
    }

    /**
     * 创建缓存实例
     *
     * @param name   缓存名称
     * @param config 缓存配置
     * @return 缓存实例
     */
    protected abstract <K, V> Cache<K, V> createCache(String name, CacheConfig config);
}
