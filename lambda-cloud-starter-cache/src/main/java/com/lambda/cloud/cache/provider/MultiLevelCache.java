package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.Cache;
import com.lambda.cloud.cache.CacheStats;
import com.lambda.cloud.cache.support.CacheMessage;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存实现
 * <p>
 * L1: Caffeine本地缓存(快速访问)
 * L2: Redis分布式缓存(共享数据)
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 */
@Slf4j
public class MultiLevelCache<K, V> implements Cache<K, V> {

    private final String name;
    /**
     * -- GETTER --
     * 获取L1缓存
     */
    @Getter
    private final Cache<K, V> l1Cache; // Caffeine
    /**
     * -- GETTER --
     * 获取L2缓存
     */
    @Getter
    private final Cache<K, V> l2Cache; // Redis

    private final org.springframework.data.redis.core.RedisTemplate<Object, Object> redisTemplate;
    private final String topic;
    private final String currentNodeId;

    public MultiLevelCache(
            String name,
            Cache<K, V> l1Cache,
            Cache<K, V> l2Cache,
            org.springframework.data.redis.core.RedisTemplate<Object, Object> redisTemplate,
            String topic,
            String currentNodeId) {
        this.name = name;
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.currentNodeId = currentNodeId;
    }

    private void publishMessage(CacheMessage.Type type, Object key) {
        publishMessage(type, key, null);
    }

    private void publishMessage(CacheMessage.Type type, Object key, Set<Object> keys) {
        try {
            CacheMessage message = new CacheMessage(name, key, currentNodeId, keys, type);
            redisTemplate.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("Failed to publish cache message", e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public V get(K key) {
        // 先从L1缓存获取
        V value = l1Cache.get(key);
        if (value != null) {
            log.debug("Cache hit in L1: key={}", key);
            return value;
        }

        // L1未命中,从L2获取
        value = l2Cache.get(key);
        if (value != null) {
            log.debug("Cache hit in L2: key={}, syncing to L1", key);
            // 同步到L1缓存
            l1Cache.put(key, value);
        }

        return value;
    }

    @Override
    public V get(K key, java.util.concurrent.Callable<V> valueLoader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        try {
            value = valueLoader.call();
            if (value != null) {
                put(key, value);
            }
            return value;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load value for key: " + key, e);
        }
    }

    @Override
    public V get(K key, Function<K, V> valueLoader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        value = valueLoader.apply(key);
        if (value != null) {
            put(key, value);
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        // 先从L1获取
        Map<K, V> result = l1Cache.getAll(keys);

        // 找出未命中的键
        Set<K> missedKeys = new java.util.HashSet<>(keys);
        missedKeys.removeAll(result.keySet());

        if (!missedKeys.isEmpty()) {
            // 从L2获取未命中的键
            Map<K, V> l2Values = l2Cache.getAll(missedKeys);
            result.putAll(l2Values);

            // 同步到L1
            if (!l2Values.isEmpty()) {
                l1Cache.putAll(l2Values);
            }
        }

        return result;
    }

    @Override
    public void put(K key, V value) {
        // 同时写入L1和L2
        l1Cache.put(key, value);
        l2Cache.put(key, value);
        publishMessage(CacheMessage.Type.PUT, key);
    }

    @Override
    public void put(K key, V value, Duration duration) {
        l1Cache.put(key, value, duration);
        l2Cache.put(key, value, duration);
        publishMessage(CacheMessage.Type.PUT, key);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        // 先尝试在L2中设置(分布式锁)
        boolean result = l2Cache.putIfAbsent(key, value);
        if (result) {
            // L2设置成功,同步到L1
            l1Cache.putIfAbsent(key, value);
            publishMessage(CacheMessage.Type.PUT, key);
        }
        return result;
    }

    @Override
    public boolean putIfAbsent(K key, V value, Duration duration) {
        boolean result = l2Cache.putIfAbsent(key, value, duration);
        if (result) {
            l1Cache.putIfAbsent(key, value, duration);
            publishMessage(CacheMessage.Type.PUT, key);
        }
        return result;
    }

    @Override
    public void putAll(Map<K, V> map) {
        l1Cache.putAll(map);
        l2Cache.putAll(map);
        publishMessage(CacheMessage.Type.PUT_ALL, null, new java.util.HashSet<>(map.keySet()));
    }

    @Override
    public void putAll(Map<K, V> map, Duration duration) {
        l1Cache.putAll(map, duration);
        l2Cache.putAll(map, duration);
        publishMessage(CacheMessage.Type.PUT_ALL, null, new java.util.HashSet<>(map.keySet()));
    }

    @Override
    public void evict(K key) {
        // 同时清除L1和L2
        l1Cache.evict(key);
        l2Cache.evict(key);
        publishMessage(CacheMessage.Type.EVICT, key);
    }

    @Override
    public void evictAll(Set<K> keys) {
        l1Cache.evictAll(keys);
        l2Cache.evictAll(keys);
        publishMessage(CacheMessage.Type.EVICT_ALL, null, new java.util.HashSet<>(keys));
    }

    @Override
    public void clear() {
        l1Cache.clear();
        l2Cache.clear();
        publishMessage(CacheMessage.Type.CLEAR, null);
    }

    @Override
    public boolean exists(K key) {
        return l1Cache.exists(key) || l2Cache.exists(key);
    }

    @Override
    public long size() {
        // 返回L2的大小(更准确)
        return l2Cache.size();
    }

    @Override
    public boolean expire(K key, Duration duration) {
        // 只在L2设置过期时间(L1使用全局TTL)
        return l2Cache.expire(key, duration);
    }

    @Override
    public Duration getExpire(K key) {
        return l2Cache.getExpire(key);
    }

    @Override
    public CacheStats getStats() {
        CacheStats l1Stats = l1Cache.getStats();
        CacheStats l2Stats = l2Cache.getStats();

        return CacheStats.builder()
                .hitCount(l1Stats.getHitCount() + l2Stats.getHitCount())
                .missCount(l1Stats.getMissCount() + l2Stats.getMissCount())
                .loadSuccessCount(l1Stats.getLoadSuccessCount() + l2Stats.getLoadSuccessCount())
                .loadFailureCount(l1Stats.getLoadFailureCount() + l2Stats.getLoadFailureCount())
                .totalLoadTime(l1Stats.getTotalLoadTime() + l2Stats.getTotalLoadTime())
                .evictionCount(l1Stats.getEvictionCount() + l2Stats.getEvictionCount())
                .size(size())
                .build();
    }

    @Override
    public Object getNativeCache() {
        return new Object[] {l1Cache.getNativeCache(), l2Cache.getNativeCache()};
    }
}
