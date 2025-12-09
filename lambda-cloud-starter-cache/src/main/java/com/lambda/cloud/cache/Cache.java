package com.lambda.cloud.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 统一缓存接口
 * <p>
 * 提供基本的缓存操作,支持多种缓存实现(Redis, Caffeine, 多级缓存等)
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 */
public interface Cache<K, V> {

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    String getName();

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值,不存在返回null
     */
    V get(K key);

    /**
     * 获取缓存值,如果不存在则通过valueLoader加载
     *
     * @param key         缓存键
     * @param valueLoader 值加载器
     * @return 缓存值
     */
    V get(K key, Callable<V> valueLoader);

    /**
     * 获取缓存值,如果不存在则通过valueLoader加载
     *
     * @param key         缓存键
     * @param valueLoader 值加载器
     * @return 缓存值
     */
    V get(K key, Function<K, V> valueLoader);

    /**
     * 批量获取缓存值
     *
     * @param keys 缓存键集合
     * @return 缓存值映射
     */
    Map<K, V> getAll(Set<K> keys);

    /**
     * 设置缓存值
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void put(K key, V value);

    /**
     * 设置缓存值,带过期时间
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param duration 过期时间
     */
    void put(K key, V value, Duration duration);

    /**
     * 如果不存在则设置缓存值
     *
     * @param key   缓存键
     * @param value 缓存值
     * @return true表示设置成功,false表示键已存在
     */
    boolean putIfAbsent(K key, V value);

    /**
     * 如果不存在则设置缓存值,带过期时间
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param duration 过期时间
     * @return true表示设置成功,false表示键已存在
     */
    boolean putIfAbsent(K key, V value, Duration duration);

    /**
     * 批量设置缓存值
     *
     * @param map 缓存键值映射
     */
    void putAll(Map<K, V> map);

    /**
     * 批量设置缓存值,带过期时间
     *
     * @param map      缓存键值映射
     * @param duration 过期时间
     */
    void putAll(Map<K, V> map, Duration duration);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    void evict(K key);

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     */
    void evictAll(Set<K> keys);

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 检查缓存键是否存在
     *
     * @param key 缓存键
     * @return true表示存在
     */
    boolean exists(K key);

    /**
     * 获取缓存大小
     *
     * @return 缓存中的条目数量
     */
    long size();

    /**
     * 设置缓存过期时间
     *
     * @param key      缓存键
     * @param duration 过期时间
     * @return true表示设置成功
     */
    boolean expire(K key, Duration duration);

    /**
     * 获取缓存剩余过期时间
     *
     * @param key 缓存键
     * @return 剩余过期时间,永不过期返回null
     */
    Duration getExpire(K key);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    CacheStats getStats();

    /**
     * 获取原生缓存对象(用于特殊场景)
     *
     * @return 原生缓存对象
     */
    Object getNativeCache();
}
