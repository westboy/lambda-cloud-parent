package com.jingfang.cloud.redis.configuration;

import org.redisson.config.Config;

/**
 * @author westboy
 */
@FunctionalInterface
public interface RedissonConfigurationCustomizer {
    /**
     * 自定义Redisson配置
     *
     * @param  config Config
     */
    void customize(Config config);
}