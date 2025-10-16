package com.lambda.cloud.netty.protocol.cache;

import lombok.Data;

/**
 * 缓存条目
 */
@Data
public class CacheEntry<V> {
    private final V value;
    private volatile long accessTime;

    public CacheEntry(V value, long accessTime) {
        this.value = value;
        this.accessTime = accessTime;
    }

    public void updateAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }
}
