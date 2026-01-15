package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.support.CacheMessage;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 多级缓存实现
 * <p>
 * L1: 本地缓存 (如 Caffeine)
 * L2: 分布式缓存 (如 Redis)
 */
@Slf4j
public class MultiLevelCache implements Cache {

    @Getter
    private final String name;

    @Getter
    private final Cache l1Cache;

    @Getter
    private final Cache l2Cache;

    private final RedisTemplate<Object, Object> redisTemplate;
    private final String topic;
    private final String currentNodeId;
    private final ConcurrentHashMap<Object, Object> keyLocks = new ConcurrentHashMap<>();

    public MultiLevelCache(
            String name,
            Cache l1Cache,
            Cache l2Cache,
            RedisTemplate<Object, Object> redisTemplate,
            String topic,
            String currentNodeId) {
        this.name = name;
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.currentNodeId = currentNodeId;
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        // 1. 尝试 L1
        ValueWrapper l1Value = l1Cache.get(key);
        if (l1Value != null) {
            debug(key, "L1");
            return l1Value;
        }

        // 2. 尝试 L2
        ValueWrapper l2Value = l2Cache.get(key);
        if (l2Value != null) {
            debug(key, "L2");
            // 同步到 L1（包括 null 值，避免空值重复穿透到 L2）
            l1Cache.put(key, l2Value.get());
            return l2Value;
        }

        return null;
    }

    private static void debug(@NonNull Object key, String cache) {
        log.debug("{} cache hit for key: {}", key, cache);
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        // 1. 尝试 L1
        T l1Value = l1Cache.get(key, type);
        if (l1Value != null) {
            debug(key, "L1");
            return l1Value;
        }

        // 2. 尝试 L2
        T l2Value = l2Cache.get(key, type);
        if (l2Value != null) {
            debug(key, "L2");
            // 同步到 L1
            l1Cache.put(key, l2Value);
            return l2Value;
        }

        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        ValueWrapper l1Value = l1Cache.get(key);
        if (l1Value != null) {
            //noinspection unchecked
            return (T) l1Value.get();
        }
        Object lock = keyLocks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            try {
                T value = l2Cache.get(key, valueLoader);
                // 如果在 L2 中加载/找到，则放入 L1
                if (value != null) {
                    l1Cache.put(key, value);
                }
                return value;
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            } finally {
                keyLocks.remove(key, lock);
            }
        }
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, Object value) {
        // 使用 L2 的 putIfAbsent 确保分布式原子性
        ValueWrapper existingValue = l2Cache.putIfAbsent(key, value);
        if (existingValue == null) {
            // 新值已放入 L2，同步到 L1 并通知其他节点
            l1Cache.put(key, value);
            publishMessage(CacheMessage.Type.PUT, key);
        }
        return existingValue;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        l2Cache.put(key, value);
        l1Cache.put(key, value);
        publishMessage(CacheMessage.Type.PUT, key);
    }

    @Override
    public void evict(@NonNull Object key) {
        l2Cache.evict(key);
        l1Cache.evict(key);
        publishMessage(CacheMessage.Type.EVICT, key);
    }

    /**
     * 批量放入
     *
     * @param map 键值对映射
     */
    public void putAll(Map<?, ?> map) {
        if (map.isEmpty()) {
            return;
        }
        // 分别放入 L2 和 L1
        map.forEach((key, value) -> {
            l2Cache.put(key, value);
            l1Cache.put(key, value);
        });
        // 发布批量 PUT 消息
        publishBatchMessage(CacheMessage.Type.PUT_ALL, map.keySet());
    }

    /**
     * 批量驱逐
     *
     * @param keys 键集合
     */
    public void evictAll(Collection<?> keys) {
        if (keys.isEmpty()) {
            return;
        }
        // 分别从 L2 和 L1 驱逐
        keys.forEach(key -> {
            l2Cache.evict(key);
            l1Cache.evict(key);
        });
        // 发布批量 EVICT 消息
        publishBatchMessage(CacheMessage.Type.EVICT_ALL, new java.util.HashSet<>(keys));
    }

    @Override
    public void clear() {
        l2Cache.clear();
        l1Cache.clear();
        publishMessage(CacheMessage.Type.CLEAR, null);
    }

    private void publishMessage(CacheMessage.Type type, Object key) {
        publishBatchMessage(type, key != null ? java.util.Collections.singleton(key) : null);
    }

    private void publishBatchMessage(CacheMessage.Type type, java.util.Collection<?> keys) {
        try {
            Object key = (keys != null && keys.size() == 1) ? keys.iterator().next() : null;
            java.util.Set<Object> keySet = (keys != null && keys.size() > 1) ? new java.util.HashSet<>(keys) : null;

            CacheMessage message = new CacheMessage(name, key, currentNodeId, keySet, type);
            redisTemplate.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("Failed to publish cache message", e);
        }
    }
}
