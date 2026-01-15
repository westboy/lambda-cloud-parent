package com.lambda.cloud.cache.support;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.lambda.cloud.cache.CacheConfig;
import java.time.Duration;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Caffeine构建工厂
 */
public class CaffeineFactory {

    /**
     * 根据配置构建Caffeine构建器
     *
     * @param config 缓存配置
     * @return Caffeine构建器
     */
    public static Caffeine<Object, Object> createCaffeine(CacheConfig config) {
        Caffeine<Object, Object> builder = getCaffeine(config);

        // 访问后过期
        if (config.getExpireAfterAccess() != null) {
            builder.expireAfterAccess(config.getExpireAfterAccess());
        }

        // 刷新后过期
        if (config.getRefreshAfterWrite() != null) {
            builder.refreshAfterWrite(config.getRefreshAfterWrite());
        }

        // 软引用值
        if (config.isSoftValues()) {
            builder.softValues();
        }

        // 弱引用值（与软引用互斥）
        if (config.isWeakValues() && !config.isSoftValues()) {
            builder.weakValues();
        }

        // 弱引用键
        if (config.isWeakKeys()) {
            builder.weakKeys();
        }

        // 统计信息
        if (config.isEnableStats()) {
            builder.recordStats();
        }

        return builder;
    }

    private static @NonNull Caffeine<Object, Object> getCaffeine(CacheConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        // 初始容量
        if (config.getInitialCapacity() > 0) {
            builder.initialCapacity(config.getInitialCapacity());
        }

        // 最大缓存大小
        if (config.getMaxSize() > 0) {
            builder.maximumSize(config.getMaxSize());
        }

        // 过期策略优先级: expireAfterWrite > l1Ttl > ttl
        Duration expireAfterWrite = config.getExpireAfterWrite();
        if (expireAfterWrite == null) {
            expireAfterWrite = config.getL1Ttl();
        }
        if (expireAfterWrite == null) {
            expireAfterWrite = config.getTtl();
        }

        if (expireAfterWrite != null) {
            builder.expireAfterWrite(expireAfterWrite);
        }
        return builder;
    }
}
