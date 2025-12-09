package com.lambda.cloud.cache;

import lombok.Getter;

/**
 * 缓存类型枚举
 */
@Getter
public enum CacheType {

    /**
     * Redis缓存
     */
    REDIS("redis", "Redis分布式缓存"),

    /**
     * Caffeine本地缓存
     */
    CAFFEINE("caffeine", "Caffeine本地缓存"),

    /**
     * 多级缓存(L1: Caffeine, L2: Redis)
     */
    MULTI_LEVEL("multi-level", "多级缓存"),

    /**
     * 自定义缓存
     */
    CUSTOM("custom", "自定义缓存");

    private final String code;
    private final String description;

    CacheType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
