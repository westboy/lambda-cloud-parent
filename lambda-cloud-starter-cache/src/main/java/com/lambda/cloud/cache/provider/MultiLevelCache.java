package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.CacheStats;
import com.lambda.cloud.cache.support.AbstractCache;
import com.lambda.cloud.cache.support.CacheMessage;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache.ValueWrapper;

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
public class MultiLevelCache<K, V> extends AbstractCache<K, V> {

    /**
     * -- GETTER --
     * 获取L1缓存
     */
    @Getter
    private final org.springframework.cache.Cache l1Cache; // Caffeine
    /**
     * -- GETTER --
     * 获取L2缓存
     */
    @Getter
    private final org.springframework.cache.Cache l2Cache; // Redis

    private final org.springframework.data.redis.core.RedisTemplate<Object, Object> redisTemplate;
    private final String topic;
    private final String currentNodeId;

    public MultiLevelCache(
            String name,
            org.springframework.cache.Cache l1Cache,
            org.springframework.cache.Cache l2Cache,
            org.springframework.data.redis.core.RedisTemplate<Object, Object> redisTemplate,
            String topic,
            String currentNodeId) {
        super(name, true); // 默认开启统计
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

    @SuppressWarnings("unchecked")
    private V toValue(ValueWrapper wrapper) {
        return wrapper != null ? (V) wrapper.get() : null;
    }

    @Override
    protected V lookupInternal(K key) {
        // 先从L1缓存获取
        ValueWrapper l1Wrapper = l1Cache.get(key);
        V value = toValue(l1Wrapper);
        if (value != null) {
            log.debug("Cache hit in L1: key={}", key);
            recordHit();
            return value;
        }

        // L1未命中,从L2获取
        ValueWrapper l2Wrapper = l2Cache.get(key);
        value = toValue(l2Wrapper);
        if (value != null) {
            log.debug("Cache hit in L2: key={}, syncing to L1", key);
            // 同步到L1缓存
            l1Cache.put(key, value);
            recordHit(); // L2 hit counts as hit for multi-level
        } else {
            recordMiss();
        }

        return value;
    }

    @Override
    protected void putInternal(K key, V value) {
        // 同时写入L1和L2
        l1Cache.put(key, value);
        l2Cache.put(key, value);
        publishMessage(CacheMessage.Type.PUT, key);
    }

    @Override
    public void put(K key, V value, Duration duration) {
        // 扩展支持 Duration
        // 尝试检查底层是否支持 Duration
        putToCache(l1Cache, key, value, duration);
        putToCache(l2Cache, key, value, duration);
        publishMessage(CacheMessage.Type.PUT, key);
    }

    private void putToCache(org.springframework.cache.Cache cache, K key, V value, Duration duration) {
        if (cache instanceof AbstractCache) {
            ((AbstractCache) cache).put(key, value, duration);
        } else {
            cache.put(key, value);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        // 先尝试在L2中设置(分布式锁)
        // Spring Cache 的 putIfAbsent 返回 ValueWrapper
        ValueWrapper wrapper = l2Cache.putIfAbsent(key, value);
        if (wrapper == null) {
            // L2设置成功 (返回null表示之前没有值), 同步到L1
            l1Cache.putIfAbsent(key, value);
            @SuppressWarnings("unchecked")
            K k = (K) key;
            publishMessage(CacheMessage.Type.PUT, k);
            return null;
        }
        return wrapper;
    }

    @Override
    public ValueWrapper putIfAbsent(K key, V value, Duration duration) {
        // 尝试扩展
        boolean l2Success = false;
        ValueWrapper result = null;

        if (l2Cache instanceof AbstractCache) {
            @SuppressWarnings("unchecked")
            AbstractCache<Object, Object> l2 = (AbstractCache<Object, Object>) l2Cache;
            result = l2.putIfAbsent(key, value, duration);
            l2Success = (result == null);
        } else {
            result = l2Cache.putIfAbsent(key, value);
            l2Success = (result == null);
        }

        if (l2Success) {
            if (l1Cache instanceof AbstractCache) {
                @SuppressWarnings("unchecked")
                AbstractCache<Object, Object> l1 = (AbstractCache<Object, Object>) l1Cache;
                l1.putIfAbsent(key, value, duration);
            } else {
                l1Cache.putIfAbsent(key, value);
            }
            publishMessage(CacheMessage.Type.PUT, key);
            return null;
        }
        return result;
    }

    @Override
    public void putAll(Map<K, V> map) {
        if (l1Cache instanceof AbstractCache)
            ((AbstractCache) l1Cache).putAll(map);
        else
            map.forEach(l1Cache::put);

        if (l2Cache instanceof AbstractCache)
            ((AbstractCache) l2Cache).putAll(map);
        else
            map.forEach(l2Cache::put);

        publishMessage(CacheMessage.Type.PUT_ALL, null, new java.util.HashSet<>(map.keySet()));
    }

    @Override
    protected void evictInternal(K key) {
        l1Cache.evict(key);
        l2Cache.evict(key);
        publishMessage(CacheMessage.Type.EVICT, key);
    }

    @Override
    public void evictAll(Set<K> keys) {
        if (l1Cache instanceof AbstractCache)
            ((AbstractCache) l1Cache).evictAll(keys);
        else
            keys.forEach(l1Cache::evict);

        if (l2Cache instanceof AbstractCache)
            ((AbstractCache) l2Cache).evictAll(keys);
        else
            keys.forEach(l2Cache::evict);

        publishMessage(CacheMessage.Type.EVICT_ALL, null, new java.util.HashSet<>(keys));
    }

    @Override
    protected void clearInternal() {
        l1Cache.clear();
        l2Cache.clear();
        publishMessage(CacheMessage.Type.CLEAR, null);
    }

    @Override
    public boolean exists(K key) {
        // 简单判断 L1 或 L2
        if (l1Cache instanceof AbstractCache && ((AbstractCache) l1Cache).exists(key))
            return true;
        if (l2Cache instanceof AbstractCache && ((AbstractCache) l2Cache).exists(key))
            return true;

        return l1Cache.get(key) != null || l2Cache.get(key) != null;
    }

    @Override
    public long size() {
        if (l2Cache instanceof AbstractCache) {
            return ((AbstractCache) l2Cache).size();
        }
        return 0;
    }

    @Override
    public boolean expire(K key, Duration duration) {
        if (l2Cache instanceof AbstractCache) {
            return ((AbstractCache) l2Cache).expire(key, duration);
        }
        return false;
    }

    @Override
    public Duration getExpire(K key) {
        if (l2Cache instanceof AbstractCache) {
            return ((AbstractCache) l2Cache).getExpire(key);
        }
        return null;
    }

    @Override
    public CacheStats getStats() {
        CacheStats l1Stats = l1Cache instanceof AbstractCache ? ((AbstractCache) l1Cache).getStats()
                : CacheStats.builder().build();
        CacheStats l2Stats = l2Cache instanceof AbstractCache ? ((AbstractCache) l2Cache).getStats()
                : CacheStats.builder().build();

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
    protected Object getNativeCacheInternal() {
        return new Object[] { l1Cache, l2Cache }; // 或者返回 this
    }
}
