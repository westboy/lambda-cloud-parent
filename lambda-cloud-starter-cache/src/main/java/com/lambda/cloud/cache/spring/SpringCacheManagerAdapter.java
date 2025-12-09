package com.lambda.cloud.cache.spring;

import com.lambda.cloud.cache.Cache;
import com.lambda.cloud.cache.CacheConfig;
import com.lambda.cloud.cache.CacheManager;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Spring CacheManager适配器
 * <p>
 * 可以使用Spring Cache注解（@Cacheable, @CacheEvict, @CachePut）
 */
@Slf4j
public class SpringCacheManagerAdapter implements org.springframework.cache.CacheManager {

    private final CacheManager delegate;
    private final ConcurrentHashMap<String, SpringCacheAdapter> springCaches = new ConcurrentHashMap<>();
    private final CacheConfigResolver configResolver;

    /**
     * 缓存配置解析器接口
     */
    @FunctionalInterface
    public interface CacheConfigResolver {
        /**
         * 根据缓存名称获取配置
         *
         * @param cacheName 缓存名称
         * @return 缓存配置
         */
        CacheConfig resolve(String cacheName);
    }

    public SpringCacheManagerAdapter(CacheManager delegate) {
        this(delegate, CacheConfig::defaultConfig);
    }

    public SpringCacheManagerAdapter(CacheManager delegate, CacheConfigResolver configResolver) {
        this.delegate = delegate;
        this.configResolver = configResolver;
    }

    @Override
    public org.springframework.cache.Cache getCache(@NonNull String name) {
        return springCaches.computeIfAbsent(name, this::createSpringCache);
    }

    private SpringCacheAdapter createSpringCache(String name) {
        log.debug("Creating Spring Cache adapter for: {}", name);
        CacheConfig config = configResolver.resolve(name);
        Cache<Object, Object> lambdaCache = delegate.getOrCreateCache(name, config);
        return new SpringCacheAdapter(lambdaCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

}