package com.lambda.cloud.cache;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheConfig {

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 默认过期时间
     */
    @Builder.Default
    private Duration ttl = Duration.ofHours(1);

    /**
     * 最大缓存条目数(仅本地缓存有效)
     */
    @Builder.Default
    private long maxSize = 10000;

    /**
     * 初始容量(仅本地缓存有效)
     */
    @Builder.Default
    private int initialCapacity = 100;

    /**
     * 是否允许空值
     */
    @Builder.Default
    private boolean allowNullValues = true;

    /**
     * 键前缀
     */
    private String keyPrefix;

    /**
     * 是否启用统计
     */
    @Builder.Default
    private boolean enableStats = true;

    /**
     * 写入后过期时间(仅本地缓存有效)
     */
    private Duration expireAfterWrite;

    /**
     * 访问后过期时间(仅本地缓存有效)
     */
    private Duration expireAfterAccess;

    /**
     * 刷新后过期时间(仅本地缓存有效)
     */
    private Duration refreshAfterWrite;

    /**
     * 软引用值(仅本地缓存有效)
     */
    @Builder.Default
    private boolean softValues = false;

    /**
     * 弱引用值(仅本地缓存有效)
     */
    @Builder.Default
    private boolean weakValues = false;

    /**
     * 弱引用键(仅本地缓存有效)
     */
    @Builder.Default
    private boolean weakKeys = false;

    /**
     * 创建默认配置
     *
     * @param cacheName 缓存名称
     * @return 缓存配置
     */
    public static CacheConfig defaultConfig(String cacheName) {
        return CacheConfig.builder().cacheName(cacheName).build();
    }
}
