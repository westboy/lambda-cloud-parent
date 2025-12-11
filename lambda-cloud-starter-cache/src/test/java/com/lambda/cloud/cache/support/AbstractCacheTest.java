package com.lambda.cloud.cache.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;

public class AbstractCacheTest {

    // Concrete implementation for testing
    static class TestCache extends AbstractCache<String, String> {
        private final java.util.Map<String, String> store = new java.util.concurrent.ConcurrentHashMap<>();

        public TestCache() {
            super("test", false);
        }

        @Override
        protected String lookupInternal(String key) {
            return store.get(key);
        }

        @Override
        protected void putInternal(String key, String value) {
            store.put(key, value);
        }

        @Override
        protected void evictInternal(String key) {
            store.remove(key);
        }

        @Override
        protected void clearInternal() {
            store.clear();
        }

        @Override
        protected Object getNativeCacheInternal() {
            return store;
        }

        // Custom method not in Spring Cache interface but was in AbstractCache?
        // If AbstractCache doesn't have expire/getExpire anymore, these are just valid
        // methods of TestCache or should be removed.
        // Assuming AbstractCache deleted them since they were part of custom interface.
    }

    @Test
    public void testLoadValueConcurrency() throws InterruptedException {
        TestCache cache = new TestCache();
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger loaderCalls = new AtomicInteger(0);

        Callable<String> loader = () -> {
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
                } catch (Exception e) {
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
        assertEquals("value", cache.get("key").get());
    }
}
