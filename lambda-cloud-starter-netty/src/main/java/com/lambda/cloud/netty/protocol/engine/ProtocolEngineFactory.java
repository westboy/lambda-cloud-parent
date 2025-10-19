package com.lambda.cloud.netty.protocol.engine;

import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议引擎工厂
 * <p>
 * 提供不同类型协议引擎的创建和管理
 * </p>
 *
 * @author Jin
 */
public class ProtocolEngineFactory {

    private static final Map<EngineType, ProtocolEngine<?>> ENGINE_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取协议引擎实例
     *
     * @param type 引擎类型
     * @param <T>  消息类型
     * @return 协议引擎实例
     */
    @SuppressWarnings("unchecked")
    public static <T> ProtocolEngine<T> getEngine(EngineType type) {
        return (ProtocolEngine<T>) ENGINE_CACHE.computeIfAbsent(type, ProtocolEngineFactory::createEngine);
    }

    /**
     * 获取默认的协议引擎（增强版，带性能监控）
     *
     * @param <T> 消息类型
     * @return 增强协议引擎实例
     */
    public static <T> ProtocolEngine<T> getDefaultEngine() {
        return getEngine(EngineType.REFLECTION);
    }

    public static void addEngine(EngineType type, ProtocolEngine<?> engine) {
        ENGINE_CACHE.put(type, engine);
    }

    /**
     * 创建协议引擎实例
     *
     * @param type 引擎类型
     * @return 协议引擎实例
     */
    private static ProtocolEngine<?> createEngine(EngineType type) {
        return switch (type) {
            case REFLECTION -> new ReflectionProtocolEngine();
            case BYTECODE -> throw new UnsupportedOperationException("字节码引擎暂未实现");
            case ANNOTATION_PROCESSOR -> throw new UnsupportedOperationException("注解处理器引擎暂未实现");
        };
    }

    /**
     * 清理缓存
     */
    public static void clearCache() {
        ENGINE_CACHE.clear();
    }

    /**
     * 清理所有引擎的缓存
     */
    public static void clearAllCaches() {
        for (ProtocolEngine<?> engine : ENGINE_CACHE.values()) {
            if (engine instanceof ReflectionProtocolEngine reflectionEngine) {
                reflectionEngine.clearCache();
            }
        }
        clearCache();
    }

    /**
     * 引擎类型枚举
     */
    public enum EngineType {
        /**
         * 反射引擎（基础版）
         */
        REFLECTION,

        /**
         * 字节码引擎（高性能）
         */
        BYTECODE,

        /**
         * 注解处理器引擎（编译时生成）
         */
        ANNOTATION_PROCESSOR
    }
}
