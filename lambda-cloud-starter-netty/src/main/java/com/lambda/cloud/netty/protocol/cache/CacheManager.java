package com.lambda.cloud.netty.protocol.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 高性能缓存管理器
 * <p>
 * 提供线程安全的LRU缓存实现，支持缓存统计和自动清理
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Jin
 */
public class CacheManager<K, V> {

    /**
     * 默认最大缓存大小
     */
    private static final int DEFAULT_MAX_SIZE = 1000;

    /**
     * 缓存存储
     */
    private final ConcurrentHashMap<K, CacheEntry<V>> cache;

    /**
     * 最大缓存大小
     */
    private final int maxSize;

    /**
     * 缓存命中次数
     */
    private final AtomicLong hitCount = new AtomicLong(0);

    /**
     * 缓存未命中次数
     */
    private final AtomicLong missCount = new AtomicLong(0);

    /**
     * 访问时间戳，用于LRU淘汰
     */
    private final AtomicLong accessTime = new AtomicLong(0);

    /**
     * 构造函数
     */
    public CacheManager() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * 构造函数
     *
     * @param maxSize 最大缓存大小
     */
    public CacheManager(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>(maxSize);
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回null
     */
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null) {
            entry.updateAccessTime(accessTime.incrementAndGet());
            hitCount.incrementAndGet();
            return entry.getValue();
        }
        missCount.incrementAndGet();
        return null;
    }

    /**
     * 获取缓存值，如果不存在则计算并缓存
     *
     * @param key      缓存键
     * @param supplier 值计算函数
     * @return 缓存值
     */
    public V computeIfAbsent(K key, Function<K, V> supplier) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null) {
            entry.updateAccessTime(accessTime.incrementAndGet());
            hitCount.incrementAndGet();
            return entry.getValue();
        }

        // 计算新值
        V value = supplier.apply(key);
        if (value != null) {
            put(key, value);
        }
        missCount.incrementAndGet();
        return value;
    }

    /**
     * 放入缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }

        // 检查缓存大小，如果超过限制则清理
        if (cache.size() >= maxSize) {
            evictLRU();
        }

        CacheEntry<V> entry = new CacheEntry<>(value, accessTime.incrementAndGet());
        cache.put(key, entry);
    }

    /**
     * 移除缓存项
     *
     * @param key 缓存键
     * @return 被移除的值
     */
    public V remove(K key) {
        CacheEntry<V> entry = cache.remove(key);
        return entry != null ? entry.getValue() : null;
    }

    /**
     * 清空缓存
     */
    public void clear() {
        cache.clear();
        hitCount.set(0);
        missCount.set(0);
        accessTime.set(0);
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    public int size() {
        return cache.size();
    }

    /**
     * 获取缓存命中率
     *
     * @return 命中率（0.0-1.0）
     */
    public double getHitRate() {
        long hits = hitCount.get();
        long total = hits + missCount.get();
        return total == 0 ? 0.0 : (double) hits / total;
    }

    /**
     * LRU淘汰策略
     */
    private void evictLRU() {
        if (cache.isEmpty()) {
            return;
        }

        // 找到最久未访问的条目
        K lruKey = null;
        long oldestTime = Long.MAX_VALUE;

        for (var entry : cache.entrySet()) {
            long accessTime = entry.getValue().getAccessTime();
            if (accessTime < oldestTime) {
                oldestTime = accessTime;
                lruKey = entry.getKey();
            }
        }

        if (lruKey != null) {
            cache.remove(lruKey);
        }
    }
}
