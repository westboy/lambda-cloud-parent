package com.lambda.cloud.cache;

import java.util.Collection;

/**
 * 缓存管理器接口
 * <p>
 * 负责管理多个缓存实例的生命周期
 */
public interface CacheManager {

    /**
     * 根据名称获取缓存
     *
     * @param name 缓存名称
     * @param <K>  缓存键类型
     * @param <V>  缓存值类型
     * @return 缓存实例,不存在返回null
     */
    <K, V> Cache<K, V> getCache(String name);

    /**
     * 根据名称获取缓存,如果不存在则创建
     *
     * @param name 缓存名称
     * @param <K>  缓存键类型
     * @param <V>  缓存值类型
     * @return 缓存实例
     */
    <K, V> Cache<K, V> getOrCreateCache(String name);

    /**
     * 根据名称和配置获取缓存,如果不存在则创建
     *
     * @param name   缓存名称
     * @param config 缓存配置
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     * @return 缓存实例
     */
    <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config);

    /**
     * 获取所有缓存名称
     *
     * @return 缓存名称集合
     */
    Collection<String> getCacheNames();

    /**
     * 销毁缓存
     *
     * @param name 缓存名称
     * @return true表示销毁成功
     */
    boolean destroyCache(String name);

    /**
     * 销毁所有缓存
     */
    void destroyAll();

    /**
     * 获取缓存类型
     *
     * @return 缓存类型
     */
    CacheType getCacheType();
}
