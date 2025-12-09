package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisCacheTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Cursor<String> cursor;

    private RedisCache<String, String> redisCache;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        CacheConfig config = new CacheConfig();
        config.setCacheName("test");
        redisCache = new RedisCache<>("test", redisTemplate, config);
    }

    @Test
    void testClear_UsesScan() {
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("test:key1", "test:key2");

        redisCache.clear();

        verify(redisTemplate).scan(any(ScanOptions.class));
        verify(redisTemplate, times(1)).delete(any(Set.class)); // Verifies batch delete
        verify(redisTemplate, never()).keys(any());
    }

    @Test
    void testSize_UsesScan() {
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, true, false); // 3 items
        when(cursor.next()).thenReturn("test:key1", "test:key2", "test:key3");

        long size = redisCache.size();

        assertEquals(3, size);
        verify(redisTemplate).scan(any(ScanOptions.class));
        verify(redisTemplate, never()).keys(any());
    }
}
