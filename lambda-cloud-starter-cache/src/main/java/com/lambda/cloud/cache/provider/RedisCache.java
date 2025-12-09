package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.support.AbstractCache;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

/**
 * Redis缓存实现
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 */
@Slf4j
public class RedisCache<K, V> extends AbstractCache<K, V> {

    private final RedisTemplate<K, V> redisTemplate;
    private final CacheConfig config;
    private final String keyPrefix;

    public RedisCache(String name, RedisTemplate<K, V> redisTemplate, CacheConfig config) {
        super(name, config.isEnableStats());
        this.redisTemplate = redisTemplate;
        this.config = config;
        this.keyPrefix = config.getKeyPrefix() != null ? config.getKeyPrefix() : name + ":";
    }

    @SuppressWarnings("unchecked")
    private K buildKey(K key) {
        if (key instanceof String) {
            return (K) (keyPrefix + key);
        }
        return key;
    }

    @Override
    public V get(K key) {
        K actualKey = buildKey(key);
        V value = redisTemplate.opsForValue().get(actualKey);
        if (value != null) {
            recordHit();
        } else {
            recordMiss();
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        // 将所有键添加前缀
        Set<K> actualKeys = new java.util.HashSet<>();
        for (K key : keys) {
            actualKeys.add(buildKey(key));
        }

        java.util.List<V> values = redisTemplate.opsForValue().multiGet(actualKeys);
        Map<K, V> result = new java.util.HashMap<>();

        if (values != null) {
            java.util.Iterator<K> keyIterator = keys.iterator();
            java.util.Iterator<V> valueIterator = values.iterator();

            while (keyIterator.hasNext() && valueIterator.hasNext()) {
                K key = keyIterator.next();
                V value = valueIterator.next();
                if (value != null) {
                    result.put(key, value);
                }
            }
        }

        recordHit();
        return result;
    }

    @Override
    public void put(K key, V value) {
        K actualKey = buildKey(key);
        if (config.getTtl() != null) {
            redisTemplate.opsForValue().set(actualKey, value, config.getTtl().toMillis(), TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(actualKey, value);
        }
    }

    @Override
    public void put(K key, V value, Duration duration) {
        K actualKey = buildKey(key);
        redisTemplate.opsForValue().set(actualKey, value, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        K actualKey = buildKey(key);
        Boolean result;
        if (config.getTtl() != null) {
            result = redisTemplate
                    .opsForValue()
                    .setIfAbsent(actualKey, value, config.getTtl().toMillis(), TimeUnit.MILLISECONDS);
        } else {
            result = redisTemplate.opsForValue().setIfAbsent(actualKey, value);
        }
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean putIfAbsent(K key, V value, Duration duration) {
        K actualKey = buildKey(key);
        Boolean result =
                redisTemplate.opsForValue().setIfAbsent(actualKey, value, duration.toMillis(), TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void putAll(Map<K, V> map) {
        Map<K, V> actualMap = new java.util.HashMap<>();
        map.forEach((key, value) -> actualMap.put(buildKey(key), value));
        redisTemplate.opsForValue().multiSet(actualMap);

        // 如果配置了TTL,需要为每个键设置过期时间
        if (config.getTtl() != null) {
            actualMap.keySet().forEach(key -> redisTemplate.expire(key, config.getTtl()));
        }
    }

    @Override
    public void putAll(Map<K, V> map, Duration duration) {
        Map<K, V> actualMap = new java.util.HashMap<>();
        map.forEach((key, value) -> actualMap.put(buildKey(key), value));
        redisTemplate.opsForValue().multiSet(actualMap);

        // 为每个键设置过期时间
        actualMap.keySet().forEach(key -> redisTemplate.expire(key, duration));
    }

    @Override
    public void evict(K key) {
        K actualKey = buildKey(key);
        redisTemplate.delete(actualKey);
        recordEviction();
    }

    @Override
    public void evictAll(Set<K> keys) {
        Set<K> actualKeys = new java.util.HashSet<>();
        for (K key : keys) {
            actualKeys.add(buildKey(key));
        }
        redisTemplate.delete(actualKeys);
        if (enableStats) {
            evictionCount.addAndGet(keys.size());
        }
    }

    @Override
    public void clear() {
        ScanOptions options =
                ScanOptions.scanOptions().match(keyPrefix + "*").count(1000).build();
        try (Cursor<K> cursor = redisTemplate.scan(options)) {
            Set<K> keysBatch = new java.util.HashSet<>();
            while (cursor.hasNext()) {
                keysBatch.add(cursor.next());
                if (keysBatch.size() >= 1000) {
                    redisTemplate.delete(keysBatch);
                    keysBatch.clear();
                }
            }
            if (!keysBatch.isEmpty()) {
                redisTemplate.delete(keysBatch);
            }
        }
    }

    @Override
    public boolean exists(K key) {
        K actualKey = buildKey(key);
        Boolean result = redisTemplate.hasKey(actualKey);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public long size() {
        long count = 0;
        ScanOptions options =
                ScanOptions.scanOptions().match(keyPrefix + "*").count(1000).build();
        try (Cursor<K> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean expire(K key, Duration duration) {
        K actualKey = buildKey(key);
        Boolean result = redisTemplate.expire(actualKey, duration);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Duration getExpire(K key) {
        K actualKey = buildKey(key);
        Long ttl = redisTemplate.getExpire(actualKey, TimeUnit.MILLISECONDS);
        if (ttl == null || ttl == -1) {
            return null; // 永不过期
        }
        if (ttl == -2) {
            return Duration.ZERO; // 键不存在
        }
        return Duration.ofMillis(ttl);
    }

    @Override
    public Object getNativeCache() {
        return redisTemplate;
    }
}
