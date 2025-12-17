package com.lambda.cloud.cache.support;

import com.lambda.cloud.cache.provider.MultiLevelCache;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.NonNull;

/**
 * 缓存消息监听器
 * <p>
 * 监听 Redis 消息以清理本地 L1 缓存
 */
@Slf4j
public class CacheMessageListener implements MessageListener {

    private final CacheManager cacheManager;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Getter
    private final String currentNodeId;

    public CacheMessageListener(CacheManager cacheManager, RedisTemplate<Object, Object> redisTemplate, String nodeId) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.currentNodeId = nodeId;
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            RedisSerializer<?> serializer = redisTemplate.getValueSerializer();
            Object body = serializer.deserialize(message.getBody());

            if (body instanceof CacheMessage) {
                handleMessage((CacheMessage) body);
            } else {
                log.warn("Received unknown message type: {}", body != null ? body.getClass() : "null");
            }
        } catch (Exception e) {
            log.error("Failed to handle cache message", e);
        }
    }

    private void handleMessage(CacheMessage msg) {
        // 忽略自己发出的消息
        if (Objects.equals(msg.getSourceNodeId(), currentNodeId)) {
            return;
        }

        log.debug("Received cache message: {}", msg);

        Cache cache = cacheManager.getCache(msg.getCacheName());
        if (cache instanceof MultiLevelCache multiLevelCache) {
            Cache l1Cache = multiLevelCache.getL1Cache();

            switch (msg.getType()) {
                case PUT:
                // 设计说明：收到 PUT 消息时执行 evict 而非 put，原因：
                // 1. CacheMessage 不包含 value 字段，无法直接更新
                // 2. evict 后首次访问会从 L2 加载最新值，保证数据一致性
                // 3. 避免在消息中传输可能较大的缓存值
                case EVICT:
                    if (msg.getKey() != null) {
                        l1Cache.evict(msg.getKey());
                        log.debug("Evicted L1 cache for key: {} in cache: {}", msg.getKey(), msg.getCacheName());
                    }
                    break;
                case PUT_ALL:
                case EVICT_ALL:
                    if (msg.getKeys() != null && !msg.getKeys().isEmpty()) {
                        msg.getKeys().forEach(l1Cache::evict);
                        log.debug(
                                "Evicted L1 cache for {} keys in cache: {}",
                                msg.getKeys().size(),
                                msg.getCacheName());
                    }
                    break;
                case CLEAR:
                    l1Cache.clear();
                    log.debug("Cleared L1 cache: {}", msg.getCacheName());
                    break;
                default:
                    break;
            }
        }
    }
}
