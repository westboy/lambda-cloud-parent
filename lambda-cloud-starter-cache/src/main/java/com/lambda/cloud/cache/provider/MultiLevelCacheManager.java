package com.lambda.cloud.cache.provider;

import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheType;
import com.lambda.cloud.cache.support.AbstractCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 多级缓存管理器
 * <p>
 * L1: Caffeine本地缓存
 * L2: Redis分布式缓存
 */
@Slf4j
public class MultiLevelCacheManager extends AbstractCacheManager {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final CacheConfig defaultL1Config;
    private final CacheConfig defaultL2Config;

    private final String topic = "lambda:cache:topic";
    private final String nodeId;

    public MultiLevelCacheManager(RedisTemplate<Object, Object> redisTemplate, String nodeId) {
        this(redisTemplate, null, null, nodeId);
    }

    public MultiLevelCacheManager(
            RedisTemplate<Object, Object> redisTemplate,
            CacheConfig defaultL1Config,
            CacheConfig defaultL2Config,
            String nodeId) {
        super(CacheType.MULTI_LEVEL);
        this.redisTemplate = redisTemplate;
        this.defaultL1Config = defaultL1Config;
        this.defaultL2Config = defaultL2Config;
        this.nodeId = nodeId;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Cache createCache(String name, CacheConfig config) {
        // 创建L1缓存(Caffeine)
        CacheConfig l1Config = defaultL1Config != null ? defaultL1Config : config;
        l1Config.setCacheName(name + ":L1");
        Cache l1Cache = new CaffeineCache<>(name + ":L1", l1Config);

        // 创建L2缓存(Redis)
        CacheConfig l2Config = defaultL2Config != null ? defaultL2Config : config;
        l2Config.setCacheName(name + ":L2");
        Cache l2Cache = new RedisCache<>(name + ":L2", redisTemplate, l2Config);

        // 创建多级缓存
        return new MultiLevelCache<>(name, l1Cache, l2Cache, redisTemplate, topic, nodeId);
    }
}
