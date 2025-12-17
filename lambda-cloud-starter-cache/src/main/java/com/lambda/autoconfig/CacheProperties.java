package com.lambda.autoconfig;

import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheType;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存配置属性
 */
@Data
@ConfigurationProperties(prefix = "lambda.cache")
public class CacheProperties {

    /**
     * 缓存类型
     */
    private CacheType type = CacheType.REDIS;

    /**
     * 默认缓存配置
     */
    private CacheConfigProperties defaults = new CacheConfigProperties();

    /**
     * 特定缓存配置(key=缓存名称)
     */
    private Map<String, CacheConfigProperties> caches = new HashMap<>();

    /**
     * 缓存配置属性
     */
    @Data
    public static class CacheConfigProperties {

        /**
         * 默认过期时间（L2 分布式缓存使用）
         */
        private Duration ttl = Duration.ofHours(1);

        /**
         * L1 本地缓存过期时间（仅多级缓存有效）
         * <p>
         * 如果未设置，则使用 ttl 值
         */
        private Duration l1Ttl;

        /**
         * 最大缓存条目数(仅本地缓存有效)
         */
        private long maxSize = 10000;

        /**
         * 初始容量(仅本地缓存有效)
         */
        private int initialCapacity = 100;

        /**
         * 是否允许空值
         */
        private boolean allowNullValues = true;

        /**
         * 键前缀
         */
        private String keyPrefix;

        /**
         * 是否启用统计
         */
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
        private boolean softValues = false;

        /**
         * 弱引用值(仅本地缓存有效)
         */
        private boolean weakValues = false;

        /**
         * 弱引用键(仅本地缓存有效)
         */
        private boolean weakKeys = false;

        /**
         * 转换为CacheConfig
         */
        public CacheConfig toCacheConfig(String cacheName) {
            return CacheConfig.builder()
                    .cacheName(cacheName)
                    .ttl(ttl)
                    .l1Ttl(l1Ttl)
                    .maxSize(maxSize)
                    .initialCapacity(initialCapacity)
                    .allowNullValues(allowNullValues)
                    .keyPrefix(keyPrefix)
                    .enableStats(enableStats)
                    .expireAfterWrite(expireAfterWrite)
                    .expireAfterAccess(expireAfterAccess)
                    .refreshAfterWrite(refreshAfterWrite)
                    .softValues(softValues)
                    .weakValues(weakValues)
                    .weakKeys(weakKeys)
                    .build();
        }
    }

    /**
     * 获取指定缓存的配置
     *
     * @param cacheName 缓存名称
     * @return 缓存配置
     */
    public CacheConfig getCacheConfig(String cacheName) {
        CacheConfigProperties config = caches.getOrDefault(cacheName, defaults);
        return config.toCacheConfig(cacheName);
    }
}
