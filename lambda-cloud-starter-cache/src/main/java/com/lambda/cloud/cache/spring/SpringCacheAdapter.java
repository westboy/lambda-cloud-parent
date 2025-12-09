package com.lambda.cloud.cache.spring;

import com.lambda.cloud.cache.Cache;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Spring Cache适配器
 * <p>
 * 使得可以使用Spring Cache注解（@Cacheable, @CacheEvict, @CachePut）
 */
@Slf4j
public class SpringCacheAdapter implements org.springframework.cache.Cache {

    private final Cache<Object, Object> delegate;

    public SpringCacheAdapter(Cache<Object, Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        Object value = delegate.get(key);
        return value != null ? () -> value : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull Object key, Class<T> type) {
        Object value = delegate.get(key);
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
        }
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        return (T) delegate.get(key, () -> {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        });
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        delegate.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, Object value) {
        boolean success = delegate.putIfAbsent(key, value);
        if (success) {
            return null; // 返回null表示之前不存在
        }
        // 返回已存在的值
        Object existingValue = delegate.get(key);
        return existingValue != null ? () -> existingValue : null;
    }

    @Override
    public void evict(@NonNull Object key) {
        delegate.evict(key);
    }

    @Override
    public boolean evictIfPresent(@NonNull Object key) {
        if (delegate.exists(key)) {
            delegate.evict(key);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean invalidate() {
        delegate.clear();
        return true;
    }

}
