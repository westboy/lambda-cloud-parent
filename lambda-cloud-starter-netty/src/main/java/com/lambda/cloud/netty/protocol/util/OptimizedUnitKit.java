package com.lambda.cloud.netty.protocol.util;

import com.lambda.cloud.netty.protocol.cache.CacheManager;
import java.util.regex.Pattern;

/**
 * 优化的数据包工具类
 * <p>
 * 提供高性能的数据验证和转换功能，使用缓存和预编译正则表达式优化性能
 * </p>
 *
 * @author Jin
 */
public final class OptimizedUnitKit {

    /**
     * 数字正则表达式（预编译）
     */
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^-?\\d+$");

    /**
     * 整数正则表达式（预编译）
     */
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");

    /**
     * 小数正则表达式（预编译）
     */
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^-?\\d+\\.\\d+$");

    /**
     * 十六进制正则表达式（预编译）
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9A-Fa-f]+$");

    /**
     * 二进制正则表达式（预编译）
     */
    private static final Pattern BINARY_PATTERN = Pattern.compile("^[01]+$");

    /**
     * 数字验证缓存
     */
    private static final CacheManager<String, Boolean> NUMERIC_CACHE = new CacheManager<>(1000);

    /**
     * 整数验证缓存
     */
    private static final CacheManager<String, Boolean> INTEGER_CACHE = new CacheManager<>(1000);

    /**
     * 小数验证缓存
     */
    private static final CacheManager<String, Boolean> DECIMAL_CACHE = new CacheManager<>(1000);

    /**
     * 十六进制验证缓存
     */
    private static final CacheManager<String, Boolean> HEX_CACHE = new CacheManager<>(1000);

    /**
     * 二进制验证缓存
     */
    private static final CacheManager<String, Boolean> BINARY_CACHE = new CacheManager<>(1000);

    /**
     * 私有构造函数，防止实例化
     */
    private OptimizedUnitKit() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 检查字符串是否为数字（包括整数和小数）
     *
     * @param str 待检查的字符串
     * @return true如果是数字，false否则
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        return NUMERIC_CACHE.computeIfAbsent(str, s -> {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
    }

    /**
     * 检查字符串是否为整数
     *
     * @param str 待检查的字符串
     * @return true如果是整数，false否则
     */
    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        return INTEGER_CACHE.computeIfAbsent(
                str, s -> INTEGER_PATTERN.matcher(s).matches());
    }

    /**
     * 检查字符串是否为小数
     *
     * @param str 待检查的字符串
     * @return true如果是小数，false否则
     */
    public static boolean isDecimal(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        return DECIMAL_CACHE.computeIfAbsent(
                str, s -> DECIMAL_PATTERN.matcher(s).matches());
    }

    /**
     * 检查字符串是否为十六进制
     *
     * @param str 待检查的字符串
     * @return true如果是十六进制，false否则
     */
    public static boolean isHex(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        return HEX_CACHE.computeIfAbsent(str, s -> HEX_PATTERN.matcher(s).matches());
    }

    /**
     * 检查字符串是否为二进制
     *
     * @param str 待检查的字符串
     * @return true如果是二进制，false否则
     */
    public static boolean isBinary(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        return BINARY_CACHE.computeIfAbsent(str, s -> BINARY_PATTERN.matcher(s).matches());
    }

    /**
     * 安全的字符串转整数
     *
     * @param str          字符串
     * @param defaultValue 默认值
     * @return 转换结果
     */
    public static int safeParseInt(String str, int defaultValue) {
        if (!isInteger(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 安全的字符串转长整数
     *
     * @param str          字符串
     * @param defaultValue 默认值
     * @return 转换结果
     */
    public static long safeParseLong(String str, long defaultValue) {
        if (!isInteger(str)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 安全的字符串转双精度浮点数
     *
     * @param str          字符串
     * @param defaultValue 默认值
     * @return 转换结果
     */
    public static double safeParseDouble(String str, double defaultValue) {
        if (!isNumeric(str)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息字符串
     */
    public static String getCacheStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("OptimizedUnitKit Cache Statistics:\n");
        sb.append("Numeric Cache: ").append(NUMERIC_CACHE.getStats()).append("\n");
        sb.append("Integer Cache: ").append(INTEGER_CACHE.getStats()).append("\n");
        sb.append("Decimal Cache: ").append(DECIMAL_CACHE.getStats()).append("\n");
        sb.append("Hex Cache: ").append(HEX_CACHE.getStats()).append("\n");
        sb.append("Binary Cache: ").append(BINARY_CACHE.getStats()).append("\n");
        return sb.toString();
    }

    /**
     * 清空所有缓存
     */
    public static void clearAllCaches() {
        NUMERIC_CACHE.clear();
        INTEGER_CACHE.clear();
        DECIMAL_CACHE.clear();
        HEX_CACHE.clear();
        BINARY_CACHE.clear();
    }
}
