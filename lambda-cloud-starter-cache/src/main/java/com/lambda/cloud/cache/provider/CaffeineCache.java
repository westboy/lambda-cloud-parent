package com.lambda.cloud.cache.provider;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.support.AbstractCache;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Caffeine本地缓存实现
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 */
@Slf4j
public class CaffeineCache<K, V> extends AbstractCache<K, V> {

    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;
    private final CacheConfig config;

    public CaffeineCache(String name, CacheConfig config) {
        super(name, config.isEnableStats());
        this.config = config;
        this.cache = buildCache(config);
    }

    private com.github.benmanes.caffeine.cache.Cache<K, V> buildCache(CacheConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        // 设置最大容量
        if (config.getMaxSize() > 0) {
            builder.maximumSize(config.getMaxSize());
        }

        // 设置初始容量
        if (config.getInitialCapacity() > 0) {
            builder.initialCapacity(config.getInitialCapacity());
        }

        // 设置写入后过期
        if (config.getExpireAfterWrite() != null) {
            builder.expireAfterWrite(config.getExpireAfterWrite());
        } else if (config.getTtl() != null) {
            builder.expireAfterWrite(config.getTtl());
        }

        // 设置访问后过期
        if (config.getExpireAfterAccess() != null) {
            builder.expireAfterAccess(config.getExpireAfterAccess());
        }

        // 设置刷新时间
        if (config.getRefreshAfterWrite() != null) {
            builder.refreshAfterWrite(config.getRefreshAfterWrite());
        }

        // 设置弱引用键
        if (config.isWeakKeys()) {
            builder.weakKeys();
        }

        // 设置弱引用值
        if (config.isWeakValues()) {
            builder.weakValues();
        }

        // 设置软引用值
        if (config.isSoftValues()) {
            builder.softValues();
        }

        // 设置统计
        if (config.isEnableStats()) {
            builder.recordStats();
        }

        // 设置移除监听器
        builder.removalListener((K key, V value, RemovalCause cause) -> {
            if (cause.wasEvicted()) {
                recordEviction();
            }
            log.debug("Cache entry removed: key={}, cause={}", key, cause);
        });

        return builder.build();
    }

    @Override
    protected V lookupInternal(K key) {
        V value = cache.getIfPresent(key);
        if (value != null) {
            recordHit();
        } else {
            recordMiss();
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        Map<K, V> result = cache.getAllPresent(keys);
        recordHit();
        return result;
    }

    @Override
    protected void putInternal(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void put(K key, V value, Duration duration) {
        // Caffeine不支持单个键的TTL,使用全局配置
        log.warn("Caffeine cache does not support per-key TTL, using global TTL configuration");
        cache.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        @SuppressWarnings("unchecked")
        V v = (V) value;

        V existingValue = cache.asMap().putIfAbsent(k, v);
        System.out.println("Caffeine putIfAbsent: key=" + k + ", existing=" + existingValue); // debug
        if (existingValue != null) {
            return toValueWrapper(existingValue);
        }
        return null; // Successfully put
    }

    @Override
    public ValueWrapper putIfAbsent(K key, V value, Duration duration) {
        log.warn("Caffeine cache does not support per-key TTL, using global TTL configuration");
        return putIfAbsent((Object) key, (Object) value);
    }

    @Override
    public void putAll(Map<K, V> map) {
        cache.putAll(map);
    }

    @Override
    protected void evictInternal(K key) {
        cache.invalidate(key);
        recordEviction();
    }

    @Override
    public void evictAll(Set<K> keys) {
        cache.invalidateAll(keys);
        if (enableStats) {
            evictionCount.addAndGet(keys.size());
        }
    }

    @Override
    protected void clearInternal() {
        cache.invalidateAll();
    }

    @Override
    public boolean exists(K key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public long size() {
        return cache.estimatedSize();
    }

    @Override
    public boolean expire(K key, Duration duration) {
        log.warn("Caffeine cache does not support per-key expiration");
        return false;
    }

    @Override
    public Duration getExpire(K key) {
        log.warn("Caffeine cache does not support per-key expiration");
        return null;
    }

    @Override
    protected Object getNativeCacheInternal() {
        return cache;
    }
}
