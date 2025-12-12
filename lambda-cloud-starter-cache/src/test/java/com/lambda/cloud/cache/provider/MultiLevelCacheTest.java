package com.lambda.cloud.cache.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.lambda.cloud.cache.support.CacheMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
public class MultiLevelCacheTest {

    @Mock
    private Cache l1Cache;

    @Mock
    private Cache l2Cache;

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    private MultiLevelCache multiLevelCache;

    private final String cacheName = "testCache";
    private final String topic = "testTopic";
    private final String nodeId = "node1";

    @BeforeEach
    void setUp() {
        multiLevelCache = new MultiLevelCache(cacheName, l1Cache, l2Cache, redisTemplate, topic, nodeId);
    }

    @Test
    void testPut_PublishesMessage() {
        // 测试 PUT 操作发布消息
        String key = "key1";
        String value = "value1";

        multiLevelCache.put(key, value);

        verify(l1Cache).put(key, value);
        verify(l2Cache).put(key, value);

        ArgumentCaptor<CacheMessage> messageCaptor = ArgumentCaptor.forClass(CacheMessage.class);
        verify(redisTemplate).convertAndSend(eq(topic), messageCaptor.capture());

        CacheMessage message = messageCaptor.getValue();
        assertEquals(cacheName, message.getCacheName());
        assertEquals(key, message.getKey());
        assertEquals(nodeId, message.getSourceNodeId());
        assertEquals(CacheMessage.Type.PUT, message.getType());
    }

    @Test
    void testEvict_PublishesMessage() {
        // 测试 EVICT 操作发布消息
        String key = "key1";

        multiLevelCache.evict(key);

        verify(l1Cache).evict(key);
        verify(l2Cache).evict(key);

        ArgumentCaptor<CacheMessage> messageCaptor = ArgumentCaptor.forClass(CacheMessage.class);
        verify(redisTemplate).convertAndSend(eq(topic), messageCaptor.capture());

        CacheMessage message = messageCaptor.getValue();
        assertEquals(CacheMessage.Type.EVICT, message.getType());
        assertEquals(key, message.getKey());
    }
}
