package com.lambda.cloud.cache.support;

import com.lambda.cloud.cache.Cache;
import com.lambda.cloud.cache.CacheStats;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
public abstract class AbstractCache<K, V> implements Cache<K, V> {

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
        this.name = name;
        this.enableStats = enableStats;
    }

    @Override
    public V get(K key, Callable<V> valueLoader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        return loadValue(key, k -> {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load value for key: " + key, e);
            }
        });
    }

    @Override
    public V get(K key, Function<K, V> valueLoader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        return loadValue(key, valueLoader);
    }

    protected V loadValue(K key, Function<K, V> valueLoader) {
        long startTime = System.nanoTime();

        // 获取或创建锁对象
        Object lock = loaderLocks.computeIfAbsent(key, k -> new Object());
        // 双重检查(Double Check)
        synchronized (lock) {
            try {

                V value = get(key);
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
                // 清理锁对象(尽管无法保证完全无竞争移除,但在高并发下computeIfAbsent能保证返回同一对象)
                // 注意: 这里不能简单remove(key), 否则可能移除其他线程刚获取的锁 只有当映射值确实是我们持有的lock对象时才移除
                loaderLocks.remove(key, lock);
            }
        }
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        Map<K, V> result = new HashMap<>();
        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public void putAll(Map<K, V> map) {
        map.forEach(this::put);
    }

    @Override
    public void putAll(Map<K, V> map, Duration duration) {
        map.forEach((key, value) -> put(key, value, duration));
    }

    @Override
    public void evictAll(Set<K> keys) {
        keys.forEach(key -> {
            evict(key);
            if (enableStats) {
                evictionCount.incrementAndGet();
            }
        });
    }

    @Override
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
