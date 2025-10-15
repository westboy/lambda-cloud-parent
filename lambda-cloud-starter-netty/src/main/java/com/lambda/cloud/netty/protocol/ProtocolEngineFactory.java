package com.lambda.cloud.netty.protocol;

import com.lambda.cloud.netty.protocol.engine.EnhancedProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ReflectionProtocolEngine;
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
        return getEngine(EngineType.ENHANCED);
    }

    /**
     * 获取基础反射协议引擎
     *
     * @param <T> 消息类型
     * @return 反射协议引擎实例
     */
    public static <T> ProtocolEngine<T> getBasicEngine() {
        return getEngine(EngineType.REFLECTION);
    }

    /**
     * 获取增强协议引擎（无性能监控）
     *
     * @param <T> 消息类型
     * @return 增强协议引擎实例
     */
    public static <T> ProtocolEngine<T> getEnhancedEngineNoMonitor() {
        return getEngine(EngineType.ENHANCED_NO_MONITOR);
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
            case ENHANCED -> new EnhancedProtocolEngine(new ReflectionProtocolEngine(), true);
            case ENHANCED_NO_MONITOR -> new EnhancedProtocolEngine(new ReflectionProtocolEngine(), false);
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
     * 获取性能统计信息
     *
     * @return 性能统计信息
     */
    public static String getPerformanceStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Protocol Engine Performance Statistics:\n");

        for (Map.Entry<EngineType, ProtocolEngine<?>> entry : ENGINE_CACHE.entrySet()) {
            EngineType type = entry.getKey();
            ProtocolEngine<?> engine = entry.getValue();

            sb.append("Engine Type: ").append(type).append("\n");

            if (engine instanceof EnhancedProtocolEngine enhancedEngine) {
                sb.append(enhancedEngine.getResourceStats()).append("\n");
            } else if (engine instanceof ReflectionProtocolEngine reflectionEngine) {
                sb.append(reflectionEngine.getCacheStats()).append("\n");
            } else {
                sb.append("No statistics available\n");
            }

            sb.append("---\n");
        }

        return sb.toString();
    }

    /**
     * 重置所有引擎的性能统计
     */
    public static void resetPerformanceStats() {
        for (ProtocolEngine<?> engine : ENGINE_CACHE.values()) {
            if (engine instanceof EnhancedProtocolEngine enhancedEngine) {
                enhancedEngine.resetPerformanceStats();
            }
        }
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
         * 增强引擎（带性能监控）
         */
        ENHANCED,

        /**
         * 增强引擎（无性能监控）
         */
        ENHANCED_NO_MONITOR,

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
