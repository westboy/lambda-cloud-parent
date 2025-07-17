package com.lambda.cloud.netty.repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SerialNumberAccessor
 */
public class SerialNumberAccessor {

    private static final ConcurrentHashMap<String, AtomicInteger> CACHE = new ConcurrentHashMap<>();

    public int get(String key) {
        AtomicInteger atomicInteger = CACHE.computeIfAbsent(key, k -> new AtomicInteger(0));
        int number = atomicInteger.incrementAndGet();
        if (number == Short.MAX_VALUE) {
            atomicInteger.set(0);
        }
        return number;
    }
}
