package com.lambda.cloud.cache.support;

import com.lambda.cloud.cache.CacheStats;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 缓存抽象基类
 * <p>
 * 提供通用的缓存操作实现和统计功能
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 */
@Slf4j
@Getter
public abstract class AbstractCache<K, V> extends AbstractValueAdaptingCache {

    @Getter
    protected final String name;
    protected final boolean enableStats;

    // 本地锁Map,用于防止缓存击穿
    private final ConcurrentHashMap<Object, Object> loaderLocks = new ConcurrentHashMap<>();

    // 统计信息
    protected final AtomicLong hitCount = new AtomicLong(0);
    protected final AtomicLong missCount = new AtomicLong(0);
    protected final AtomicLong loadSuccessCount = new AtomicLong(0);
    protected final AtomicLong loadFailureCount = new AtomicLong(0);
    protected final AtomicLong totalLoadTime = new AtomicLong(0);
    protected final AtomicLong evictionCount = new AtomicLong(0);

    protected AbstractCache(String name, boolean enableStats) {
        super(true); // 允许null值
        this.name = name;
        this.enableStats = enableStats;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final Object getNativeCache() {
        return getNativeCacheInternal();
    }

    protected abstract Object getNativeCacheInternal();

    @Override
    protected Object lookup(@NonNull Object key) {
        // 抽象方法由子类实现实际查找逻辑
        return lookupInternal((K) key);
    }

    protected abstract V lookupInternal(K key);

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        // 使用Spring的ValueLoader适配逻辑，或者保留我们的锁逻辑
        // 因为AbstractValueAdaptingCache没有处理并发锁，我们保留自己的loaderLocks逻辑
        return (T) loadValue((K) key, k -> {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected Object loadValue(K key, Function<K, ?> valueLoader) {
        long startTime = System.nanoTime();

        // 获取或创建锁对象
        Object lock = loaderLocks.computeIfAbsent(key, k -> new Object());
        // 双重检查(Double Check)
        synchronized (lock) {
            try {
                Object value = lookup(key);
                if (value != null) {
                    return value;
                }

                value = valueLoader.apply(key);
                if (value != null) {
                    put(key, value);
                    if (enableStats) {
                        loadSuccessCount.incrementAndGet();
                        totalLoadTime.addAndGet(System.nanoTime() - startTime);
                    }
                }
                return value;
            } catch (Exception e) {
                if (enableStats) {
                    loadFailureCount.incrementAndGet();
                    totalLoadTime.addAndGet(System.nanoTime() - startTime);
                }
                log.error("Failed to load value for key: {}", key, e);
                throw e;
            } finally {
                loaderLocks.remove(key, lock);
            }
        }
    }

    public void putAll(Map<K, V> map) {
        map.forEach(this::put);
    }

    public void putAll(Map<K, V> map, Duration duration) {
        // 默认不支持Duration, 子类覆盖
        putAll(map);
    }

    @Override
    public void put(Object key, Object value) {
        putInternal((K) key, (V) value);
    }

    protected abstract void putInternal(K key, V value);

    // 扩展方法: 支持过期时间 (不是Spring接口的一部分)
    public void put(K key, V value, Duration duration) {
        putInternal(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        // Spring 语义：如果存在则返回现有值，否则设置并返回null
        Object existing = lookup(key);
        if (existing != null) {
            return toValueWrapper(existing);
        }
        put(key, value);
        return null;
    }

    // 扩展方法
    public ValueWrapper putIfAbsent(K key, V value, Duration duration) {
        return putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {
        evictInternal((K) key);
    }

    protected abstract void evictInternal(K key);

    public void evictAll(Set<K> keys) {
        keys.forEach(key -> {
            evict(key);
            if (enableStats) {
                evictionCount.incrementAndGet();
            }
        });
    }

    @Override
    public boolean evictIfPresent(Object key) {
        evict(key);
        return false;
    }

    @Override
    public void clear() {
        clearInternal();
    }

    protected abstract void clearInternal();

    // 扩展方法
    public boolean exists(K key) {
        return lookup(key) != null;
    }

    public long size() {
        return 0;
    }

    public boolean expire(K key, Duration duration) {
        return false;
    }

    public Duration getExpire(K key) {
        return null;
    }

    public CacheStats getStats() {
        return CacheStats.builder()
                .hitCount(hitCount.get())
                .missCount(missCount.get())
                .loadSuccessCount(loadSuccessCount.get())
                .loadFailureCount(loadFailureCount.get())
                .totalLoadTime(totalLoadTime.get())
                .evictionCount(evictionCount.get())
                .size(size())
                .build();
    }

    protected void recordHit() {
        if (enableStats) {
            hitCount.incrementAndGet();
        }
    }

    protected void recordMiss() {
        if (enableStats) {
            missCount.incrementAndGet();
        }
    }

    protected void recordEviction() {
        if (enableStats) {
            evictionCount.incrementAndGet();
        }
    }
}
