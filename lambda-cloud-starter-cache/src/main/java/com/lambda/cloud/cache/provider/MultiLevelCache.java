package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.support.CacheMessage;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        // 1. 尝试 L1
        ValueWrapper l1Value = l1Cache.get(key);
        if (l1Value != null) {
            log.debug("L1 cache hit for key: {}", key);
            return l1Value;
        }

        // 2. 尝试 L2
        ValueWrapper l2Value = l2Cache.get(key);
        if (l2Value != null) {
            log.debug("L2 cache hit for key: {}", key);
            // 同步到 L1（包括 null 值，避免空值重复穿透到 L2）
            l1Cache.put(key, l2Value.get());
            return l2Value;
        }

        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        // 1. 尝试 L1
        T l1Value = l1Cache.get(key, type);
        if (l1Value != null) {
            log.debug("L1 cache hit for key: {}", key);
            return l1Value;
        }

        // 2. 尝试 L2
        T l2Value = l2Cache.get(key, type);
        if (l2Value != null) {
            log.debug("L2 cache hit for key: {}", key);
            // 同步到 L1
            l1Cache.put(key, l2Value);
            return l2Value;
        }

        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        // 注意: 此实现已简化，可能无法保证跨级别的原子性
        ValueWrapper l1Value = l1Cache.get(key);
        if (l1Value != null) {
            return (T) l1Value.get();
        }

        // 尝试从 L2 获取或加载
        // 我们使用 L2 的 get 与加载器来确保 L2 的原子性（如果支持）
        try {
            T value = l2Cache.get(key, valueLoader);
            // 如果在 L2 中加载/找到，则放入 L1
            if (value != null) {
                l1Cache.put(key, value);
            }
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
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
    public void put(Object key, Object value) {
        l2Cache.put(key, value);
        l1Cache.put(key, value);
        publishMessage(CacheMessage.Type.PUT, key);
    }

    @Override
    public void evict(Object key) {
        l2Cache.evict(key);
        l1Cache.evict(key);
        publishMessage(CacheMessage.Type.EVICT, key);
    }

    @Override
    public void clear() {
        l2Cache.clear();
        l1Cache.clear();
        publishMessage(CacheMessage.Type.CLEAR, null);
    }

    private void publishMessage(CacheMessage.Type type, Object key) {
        try {
            CacheMessage message = new CacheMessage(name, key, currentNodeId, null, type);
            redisTemplate.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("Failed to publish cache message", e);
        }
    }
}
