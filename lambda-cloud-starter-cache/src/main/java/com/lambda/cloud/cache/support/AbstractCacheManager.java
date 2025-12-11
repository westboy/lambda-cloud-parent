package com.lambda.cloud.cache.support;

import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheType;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 缓存管理器抽象基类
 */
@Slf4j
public abstract class AbstractCacheManager implements CacheManager {

    protected final ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<>();
    protected final CacheType cacheType;

    protected AbstractCacheManager(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, key -> createCache(name, CacheConfig.defaultConfig(name)));
    }

    // 扩展方法：允许传递配置
    public Cache getOrCreateCache(String name, CacheConfig config) {
        return cacheMap.computeIfAbsent(name, key -> {
            log.info("Creating cache: name={}, type={}", name, cacheType);
            return createCache(name, config);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }

    public boolean destroyCache(String name) {
        Cache cache = cacheMap.remove(name);
        if (cache != null) {
            cache.clear();
            log.info("Cache destroyed: name={}", name);
            return true;
        }
        return false;
    }

    public void destroyAll() {
        cacheMap.values().forEach(Cache::clear);
        cacheMap.clear();
        log.info("All caches destroyed");
    }

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
    protected abstract Cache createCache(String name, CacheConfig config);
}
