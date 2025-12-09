package com.lambda.cloud.cache;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.cache.provider.CaffeineCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 缓存管理器测试
 */
class CacheManagerTest {

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new CaffeineCacheManager();
    }

    @Test
    void testGetOrCreateCache() {
        Cache<String, String> cache1 = cacheManager.getOrCreateCache("cache1");
        assertNotNull(cache1);
        assertEquals("cache1", cache1.getName());

        Cache<String, String> cache2 = cacheManager.getOrCreateCache("cache1");
        assertSame(cache1, cache2); // 应该返回同一个实例
    }

    @Test
    void testGetCacheNames() {
        cacheManager.getOrCreateCache("cache1");
        cacheManager.getOrCreateCache("cache2");

        assertEquals(2, cacheManager.getCacheNames().size());
        assertTrue(cacheManager.getCacheNames().contains("cache1"));
        assertTrue(cacheManager.getCacheNames().contains("cache2"));
    }

    @Test
    void testDestroyCache() {
        Cache<String, String> cache = cacheManager.getOrCreateCache("cache1");
        cache.put("key1", "value1");

        assertTrue(cacheManager.destroyCache("cache1"));
        assertNull(cacheManager.getCache("cache1"));
    }

    @Test
    void testDestroyAll() {
        cacheManager.getOrCreateCache("cache1");
        cacheManager.getOrCreateCache("cache2");

        cacheManager.destroyAll();
        assertEquals(0, cacheManager.getCacheNames().size());
    }

    @Test
    void testGetCacheType() {
        assertEquals(CacheType.CAFFEINE, cacheManager.getCacheType());
    }
}
