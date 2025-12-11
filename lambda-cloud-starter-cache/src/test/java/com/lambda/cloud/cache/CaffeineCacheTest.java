package com.lambda.cloud.cache;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.cache.provider.CaffeineCache;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * Caffeine缓存测试
 */
class CaffeineCacheTest {

    private CaffeineCache<String, String> cache;

    @BeforeEach
    void setUp() {
        CacheConfig config = CacheConfig.builder()
                .cacheName("testCache")
                .ttl(Duration.ofSeconds(60))
                .maxSize(100)
                .enableStats(true)
                .build();
        cache = new CaffeineCache<>("testCache", config);
    }

    @Test
    void testPutAndGet() {
        cache.put("key1", "value1");
        ValueWrapper wrapper = cache.get("key1");
        assertNotNull(wrapper);
        assertEquals("value1", wrapper.get());
    }

    @Test
    void testGetWithLoader() {
        String value = cache.get("key1", () -> "loaded-value");
        assertEquals("loaded-value", value);
        ValueWrapper wrapper = cache.get("key1");
        assertNotNull(wrapper);
        assertEquals("loaded-value", wrapper.get());
    }

    @Test
    void testPutIfAbsent() {
        ValueWrapper wrapper1 = cache.putIfAbsent("key1", "value1");
        assertNull(wrapper1); // First put returns null (or empty wrapper depending on impl, usually null if
                              // absent)

        ValueWrapper wrapper2 = cache.putIfAbsent("key1", "value2");
        assertNotNull(wrapper2);
        assertEquals("value1", wrapper2.get());

        assertEquals("value1", cache.get("key1").get());
    }

    @Test
    void testGetAll() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        Map<String, String> result = cache.getAll(Set.of("key1", "key2", "key3"));
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    void testPutAll() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        cache.putAll(map);
        assertEquals("value1", cache.get("key1").get());
        assertEquals("value2", cache.get("key2").get());
    }

    @Test
    void testEvict() {
        cache.put("key1", "value1");
        assertNotNull(cache.get("key1"));

        cache.evict("key1");
        assertNull(cache.get("key1"));
    }

    @Test
    void testEvictAll() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        cache.evictAll(Set.of("key1", "key2"));
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));
    }

    @Test
    void testClear() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        cache.clear();
        assertNull(cache.get("key1"));
    }

    @Test
    void testSize() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        assertEquals(2, cache.size());
    }

    @Test
    void testStats() {
        cache.get("key1"); // miss
        cache.put("key1", "value1");
        cache.get("key1"); // hit

        CacheStats stats = cache.getStats();
        assertEquals(1, stats.getHitCount());
        assertEquals(1, stats.getMissCount());
        assertEquals(0.5, stats.hitRate(), 0.01);
    }

    @Test
    void testGetName() {
        assertEquals("testCache", cache.getName());
    }
}
