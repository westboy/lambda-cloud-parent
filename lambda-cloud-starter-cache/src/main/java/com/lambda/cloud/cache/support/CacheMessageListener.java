package com.lambda.cloud.cache.support;

import com.lambda.cloud.cache.Cache;
import com.lambda.cloud.cache.CacheManager;
import com.lambda.cloud.cache.provider.MultiLevelCache;
import java.util.Objects;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 缓存消息监听器
 * <p>
 * 监听Redis发布的消息,清理本地L1缓存
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
        // 如果是自己发出的消息,忽略
        if (Objects.equals(msg.getSourceNodeId(), currentNodeId)) {
            return;
        }

        log.debug("Received cache message: {}", msg);

        Cache<Object, Object> cache = cacheManager.getCache(msg.getCacheName());
        if (cache instanceof MultiLevelCache<Object, Object> multiLevelCache) {
            Cache<Object, Object> l1Cache = multiLevelCache.getL1Cache();

            switch (msg.getType()) {
                case PUT:
                case EVICT:
                    if (msg.getKey() != null) {
                        l1Cache.evict(msg.getKey());
                        log.debug("Evicted L1 cache for key: {} in cache: {}", msg.getKey(), msg.getCacheName());
                    }
                    break;
                case PUT_ALL:
                case EVICT_ALL:
                    if (msg.getKeys() != null && !msg.getKeys().isEmpty()) {
                        l1Cache.evictAll(msg.getKeys());
                        log.debug("Evicted L1 cache for {} keys in cache: {}", msg.getKeys().size(),
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
