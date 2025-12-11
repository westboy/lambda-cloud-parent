package com.lambda.cloud.cache.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.lambda.cloud.cache.support.AbstractCache;
import com.lambda.cloud.cache.support.CacheMessage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
public class MultiLevelCacheTest {

    @Mock
    private AbstractCache<String, String> l1Cache;

    @Mock
    private AbstractCache<String, String> l2Cache;

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    private MultiLevelCache<String, String> multiLevelCache;

    private final String cacheName = "testCache";
    private final String topic = "testTopic";
    private final String nodeId = "node1";

    @BeforeEach
    void setUp() {
        multiLevelCache = new MultiLevelCache<>(cacheName, l1Cache, l2Cache, redisTemplate, topic, nodeId);
    }

    @Test
    void testPut_PublishesMessage() {
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

    @Test
    void testPutAll_PublishesBatchMessage() {
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        multiLevelCache.putAll(map);

        verify(l1Cache).putAll(map);
        verify(l2Cache).putAll(map);

        ArgumentCaptor<CacheMessage> messageCaptor = ArgumentCaptor.forClass(CacheMessage.class);
        verify(redisTemplate).convertAndSend(eq(topic), messageCaptor.capture());

        CacheMessage message = messageCaptor.getValue();
        assertEquals(CacheMessage.Type.PUT_ALL, message.getType());
        assertNotNull(message.getKeys());
        assertEquals(2, message.getKeys().size());
        assertTrue(message.getKeys().contains("k1"));
        assertTrue(message.getKeys().contains("k2"));
    }

    @Test
    void testEvictAll_PublishesBatchMessage() {
        Set<String> keys = new HashSet<>();
        keys.add("k1");
        keys.add("k2");

        multiLevelCache.evictAll(keys);

        verify(l1Cache).evictAll(keys);
        verify(l2Cache).evictAll(keys);

        ArgumentCaptor<CacheMessage> messageCaptor = ArgumentCaptor.forClass(CacheMessage.class);
        verify(redisTemplate).convertAndSend(eq(topic), messageCaptor.capture());

        CacheMessage message = messageCaptor.getValue();
        assertEquals(CacheMessage.Type.EVICT_ALL, message.getType());
        assertNotNull(message.getKeys());
        assertEquals(2, message.getKeys().size());
    }
}
