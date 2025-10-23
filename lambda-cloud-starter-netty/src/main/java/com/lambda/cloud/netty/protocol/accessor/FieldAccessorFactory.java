package com.lambda.cloud.netty.protocol.accessor;

import com.lambda.cloud.netty.protocol.accessor.impl.ByteCodeFieldAccessor;
import com.lambda.cloud.netty.protocol.accessor.impl.ReflectionFieldAccessor;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;

/**
 * 字段访问器工厂类
 * <p>
 * 提供统一的字段访问器创建接口，支持多种访问器类型的策略选择。
 * 包括反射访问器和字节码访问器，可根据性能需求和运行环境选择最适合的实现。
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
public class FieldAccessorFactory {

    /**
     * 访问器类型枚举
     */
    public enum AccessorType {
        /**
         * 基于 MethodHandle 的反射访问器
         * 兼容性好，性能中等
         */
        REFLECTION,

        /**
         * 基于 ASM 字节码生成的访问器
         * 性能最高，但有一定的内存开销
         */
        BYTECODE
    }

    /**
     * 默认访问器类型
     */
    private static volatile AccessorType defaultAccessorType = AccessorType.BYTECODE;

    /**
     * 访问器缓存，避免重复创建
     * Key: 类名#字段名, Value: 字段访问器
     */
    private static final ConcurrentHashMap<String, FieldAccessor> ACCESSOR_CACHE = new ConcurrentHashMap<>();

    /**
     * 是否启用缓存
     */
    private static volatile boolean cacheEnabled = true;

    /**
     * 设置是否启用缓存
     *
     * @param enabled 是否启用缓存
     */
    public static void setCacheEnabled(boolean enabled) {
        cacheEnabled = enabled;
        if (!enabled) {
            clearCache();
        }
    }

    /**
     * 使用默认类型创建字段访问器
     *
     * @param field 目标字段
     * @return 字段访问器
     */
    public static FieldAccessor createAccessor(Field field) {
        return createAccessor(field, defaultAccessorType);
    }

    /**
     * 创建指定类型的字段访问器
     *
     * @param field 目标字段
     * @param type  访问器类型
     * @return 字段访问器
     */
    public static FieldAccessor createAccessor(Field field, AccessorType type) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("AccessorType cannot be null");
        }

        // 如果启用缓存，先尝试从缓存获取
        if (cacheEnabled) {
            String cacheKey = generateCacheKey(field, type);
            FieldAccessor cached = ACCESSOR_CACHE.get(cacheKey);
            if (cached != null) {
                return cached;
            }

            // 缓存中没有，创建新的访问器并缓存
            FieldAccessor accessor = doCreateAccessor(field, type);
            ACCESSOR_CACHE.put(cacheKey, accessor);
            return accessor;
        } else {
            // 不使用缓存，直接创建
            return doCreateAccessor(field, type);
        }
    }

    /**
     * 实际创建访问器的方法
     *
     * @param field 目标字段
     * @param type  访问器类型
     * @return 字段访问器
     */
    private static FieldAccessor doCreateAccessor(Field field, AccessorType type) {
        try {
            return switch (type) {
                case REFLECTION -> new ReflectionFieldAccessor(field);
                case BYTECODE -> new ByteCodeFieldAccessor(field);
            };
        } catch (Exception e) {
            // 如果字节码生成失败，降级到反射访问器
            if (type == AccessorType.BYTECODE) {
                try {
                    return new ReflectionFieldAccessor(field);
                } catch (Exception fallbackException) {
                    throw new RuntimeException(
                            "Failed to create both bytecode and reflection accessor for field: "
                                    + field.getDeclaringClass().getName() + "#" + field.getName(),
                            fallbackException);
                }
            }
            throw new RuntimeException(
                    "Failed to create accessor for field: "
                            + field.getDeclaringClass().getName() + "#" + field.getName(),
                    e);
        }
    }

    /**
     * 生成缓存键
     *
     * @param field 目标字段
     * @param type  访问器类型
     * @return 缓存键
     */
    private static String generateCacheKey(Field field, AccessorType type) {
        return field.getDeclaringClass().getName() + "#" + field.getName() + "@" + type.name();
    }

    /**
     * 清空访问器缓存
     */
    public static void clearCache() {
        ACCESSOR_CACHE.clear();
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存中访问器的数量
     */
    public static int getCacheSize() {
        return ACCESSOR_CACHE.size();
    }

    /**
     * 检查指定字段是否已缓存
     *
     * @param field 目标字段
     * @param type  访问器类型
     * @return 是否已缓存
     */
    public static boolean isCached(Field field, AccessorType type) {
        if (!cacheEnabled) {
            return false;
        }
        String cacheKey = generateCacheKey(field, type);
        return ACCESSOR_CACHE.containsKey(cacheKey);
    }

    /**
     * 批量创建字段访问器
     *
     * @param fields 字段数组
     * @param type   访问器类型
     * @return 字段访问器数组
     */
    public static FieldAccessor[] createAccessors(Field[] fields, AccessorType type) {
        if (fields == null) {
            throw new IllegalArgumentException("Fields array cannot be null");
        }

        FieldAccessor[] accessors = new FieldAccessor[fields.length];
        for (int i = 0; i < fields.length; i++) {
            accessors[i] = createAccessor(fields[i], type);
        }
        return accessors;
    }

    /**
     * 批量创建字段访问器（使用默认类型）
     *
     * @param fields 字段数组
     * @return 字段访问器数组
     */
    public static FieldAccessor[] createAccessors(Field[] fields) {
        return createAccessors(fields, defaultAccessorType);
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息字符串
     */
    public static String getCacheStats() {
        return String.format(
                "FieldAccessorFactory Cache Stats: size=%d, enabled=%s, defaultType=%s",
                getCacheSize(), cacheEnabled, defaultAccessorType);
    }

    /**
     * 私有构造函数，防止实例化
     */
    private FieldAccessorFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
