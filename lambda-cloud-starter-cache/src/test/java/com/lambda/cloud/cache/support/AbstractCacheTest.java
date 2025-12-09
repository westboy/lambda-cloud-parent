package com.lambda.cloud.cache.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class AbstractCacheTest {

    // Concrete implementation for testing
    static class TestCache extends AbstractCache<String, String> {
        private final java.util.Map<String, String> store = new java.util.concurrent.ConcurrentHashMap<>();

        public TestCache() {
            super("test", true);
        }

        @Override
        public String get(String key) {
            return store.get(key);
        }

        @Override
        public void put(String key, String value) {
            store.put(key, value);
        }

        @Override
        public void put(String key, String value, Duration duration) {
            store.put(key, value);
        }

        @Override
        public boolean putIfAbsent(String key, String value) {
            return store.putIfAbsent(key, value) == null;
        }

        @Override
        public boolean putIfAbsent(String key, String value, Duration duration) {
            return store.putIfAbsent(key, value) == null;
        }

        @Override
        public void evict(String key) {
            store.remove(key);
        }

        @Override
        public void clear() {
            store.clear();
        }

        @Override
        public boolean exists(String key) {
            return store.containsKey(key);
        }

        @Override
        public long size() {
            return store.size();
        }

        @Override
        public boolean expire(String key, Duration duration) {
            return true;
        }

        @Override
        public Duration getExpire(String key) {
            return Duration.ZERO;
        }

        @Override
        public Object getNativeCache() {
            return store;
        }
    }

    @Test
    public void testLoadValueConcurrency() throws InterruptedException {
        TestCache cache = new TestCache();
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger loaderCalls = new AtomicInteger(0);

        Function<String, String> loader = k -> {
            loaderCalls.incrementAndGet();
            try {
                // Simulate slow loading
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "value";
        };

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    cache.get("key", loader);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads
        doneLatch.await(); // Wait for all to finish
        executor.shutdown();

        assertEquals(1, loaderCalls.get(), "Loader should be called exactly once");
        assertEquals("value", cache.get("key"));
    }
}
