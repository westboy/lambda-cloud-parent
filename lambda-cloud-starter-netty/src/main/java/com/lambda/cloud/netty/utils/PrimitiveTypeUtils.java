package com.lambda.cloud.netty.utils;

/**
 * 基本类型工具类
 * <p>
 * 提供通用的基本类型判断方法，消除重复的判断逻辑
 * </p>
 *
 * @author Jin
 */
public final class PrimitiveTypeUtils {
    /**
     * 检查字段类型是否为整型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为整型
     */
    public static boolean isIntegerType(Class<?> fieldType) {
        return fieldType == Integer.class || fieldType == int.class;
    }

    /**
     * 检查字段类型是否为长整型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为长整型
     */
    public static boolean isLongType(Class<?> fieldType) {
        return fieldType == Long.class || fieldType == long.class;
    }

    /**
     * 检查字段类型是否为短整型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为短整型
     */
    public static boolean isShortType(Class<?> fieldType) {
        return fieldType == Short.class || fieldType == short.class;
    }

    /**
     * 检查字段类型是否为字节型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为字节型
     */
    public static boolean isByteType(Class<?> fieldType) {
        return fieldType == Byte.class || fieldType == byte.class;
    }

    /**
     * 检查字段类型是否为布尔型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为布尔型
     */
    public static boolean isBooleanType(Class<?> fieldType) {
        return fieldType == Boolean.class || fieldType == boolean.class;
    }
}
