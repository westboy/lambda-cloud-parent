package com.lambda.cloud.netty.protocol.packet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据包编码解码时需要的工具包
 */
public class UnitKit {
    // 预编译的正则表达式模式
    public static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]*$");
    public static final Pattern DECIMAL_PATTERN = Pattern.compile("^[0-9]*\\.?[0-9]*$");
    public static final Pattern HEX_PAIR_PATTERN = Pattern.compile("\\w{2}");

    // 缓存机制
    private static final ConcurrentHashMap<String, String> INVERSION_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> NUMERIC_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> DECIMAL_CACHE = new ConcurrentHashMap<>();

    // 缓存大小限制
    private static final int MAX_CACHE_SIZE = 1000;

    /**
     * 判断字符串是否为数字（支持缓存）
     *
     * @param str 要判断的字符串
     * @return boolean 是否为数字
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // 检查缓存
        Boolean cached = NUMERIC_CACHE.get(str);
        if (cached != null) {
            return cached;
        }

        // 性能优化：先进行快速字符检查
        boolean result = isNumericFast(str);

        // 缓存结果（控制缓存大小）
        if (NUMERIC_CACHE.size() < MAX_CACHE_SIZE) {
            NUMERIC_CACHE.put(str, result);
        }

        return result;
    }

    /**
     * 快速数字检查（不使用正则表达式）
     *
     * @param str 要检查的字符串
     * @return boolean 是否为数字
     */
    private static boolean isNumericFast(String str) {
        boolean hasDot = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '.') {
                if (hasDot) {
                    return false; // 多个小数点
                }
                hasDot = true;
            } else if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为整数
     *
     * @param str 要判断的字符串
     * @return boolean 是否为整数
     */
    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // 整数不能包含小数点
        if (str.contains(".")) {
            return false;
        }

        return isNumeric(str);
    }

    /**
     * 判断是否为小数（支持缓存）
     *
     * @param str 要判断的字符串
     * @return boolean 是否为小数
     */
    public static boolean isDecimals(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // 检查缓存
        Boolean cached = DECIMAL_CACHE.get(str);
        if (cached != null) {
            return cached;
        }

        // 性能优化：直接检查是否包含小数点
        boolean result = false;
        if (str.contains(".")) {
            // 验证小数格式
            result = isValidDecimal(str);
        }

        // 缓存结果（控制缓存大小）
        if (DECIMAL_CACHE.size() < MAX_CACHE_SIZE) {
            DECIMAL_CACHE.put(str, result);
        }

        return result;
    }

    /**
     * 验证小数格式
     *
     * @param str 要验证的字符串
     * @return boolean 是否为有效小数
     */
    private static boolean isValidDecimal(String str) {
        int dotIndex = str.indexOf('.');
        if (dotIndex == -1) {
            return false; // 没有小数点
        }

        // 检查是否只有一个小数点
        if (str.indexOf('.', dotIndex + 1) != -1) {
            return false; // 多个小数点
        }

        // 检查小数点前后的字符
        String beforeDot = str.substring(0, dotIndex);
        String afterDot = str.substring(dotIndex + 1);

        // 小数点前可以为空或数字
        if (!beforeDot.isEmpty() && isAllDigits(beforeDot)) {
            return false;
        }

        // 小数点后必须有数字
        return !afterDot.isEmpty() && !isAllDigits(afterDot);
    }

    /**
     * 检查字符串是否全为数字字符
     *
     * @param str 要检查的字符串
     * @return boolean 是否全为数字
     */
    private static boolean isAllDigits(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return true;
            }
        }
        return false;
    }

    /**
     * 将字符串按高低位的逻辑翻转（支持缓存和性能优化）
     *
     * @param data 要翻转的字符串
     * @return String 翻转的结果
     */
    public static String highAndLowInversion(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        // 检查缓存
        String cached = INVERSION_CACHE.get(data);
        if (cached != null) {
            return cached;
        }

        String result;

        // 性能优化：根据数据长度选择不同的处理方式
        if (data.length() <= 16) {
            // 短字符串：直接处理，避免正则表达式开销
            result = highAndLowInversionFast(data);
        } else {
            // 长字符串：使用优化的正则表达式处理
            result = highAndLowInversionRegex(data);
        }

        // 缓存结果（控制缓存大小）
        if (INVERSION_CACHE.size() < MAX_CACHE_SIZE) {
            INVERSION_CACHE.put(data, result);
        }

        return result;
    }

    /**
     * 快速高低位翻转（适用于短字符串）
     *
     * @param data 要翻转的字符串
     * @return String 翻转结果
     */
    private static String highAndLowInversionFast(String data) {
        if (data.length() % 2 != 0) {
            throw new IllegalArgumentException("Data length must be even for byte pair inversion");
        }

        StringBuilder result = new StringBuilder(data.length());

        // 从后往前，每两个字符为一组
        for (int i = data.length() - 2; i >= 0; i -= 2) {
            result.append(data.charAt(i)).append(data.charAt(i + 1));
        }

        return result.toString();
    }

    /**
     * 使用正则表达式的高低位翻转（适用于长字符串）
     *
     * @param data 要翻转的字符串
     * @return String 翻转结果
     */
    private static String highAndLowInversionRegex(String data) {
        Matcher matcher = HEX_PAIR_PATTERN.matcher(data);
        StringBuilder result = new StringBuilder(data.length());

        // 收集所有匹配的字节对
        String[] pairs = new String[data.length() / 2];
        int index = 0;
        while (matcher.find() && index < pairs.length) {
            pairs[index++] = matcher.group();
        }

        // 反向拼接
        for (int i = index - 1; i >= 0; i--) {
            result.append(pairs[i]);
        }

        return result.toString();
    }

    /**
     * 将BigInteger转换为数字字符串（支持偏移和精度）
     *
     * @param offset    偏移量
     * @param precision 精度
     * @param data      数据
     * @return String 转换结果
     */
    public static String toNumString(int offset, int precision, BigInteger data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        BigDecimal bigDecimal = new BigDecimal(data);

        if (offset > 0) {
            bigDecimal = bigDecimal.subtract(BigDecimal.valueOf(offset));
        }

        if (precision > 0) {
            bigDecimal = bigDecimal.divide(new BigDecimal(10).pow(precision), precision, RoundingMode.UNNECESSARY);
            return bigDecimal.toString();
        }

        return bigDecimal.toBigInteger().toString();
    }

    /**
     * 清空所有缓存
     */
    public static void clearAllCaches() {
        INVERSION_CACHE.clear();
        NUMERIC_CACHE.clear();
        DECIMAL_CACHE.clear();
    }

    /**
     * 获取缓存统计信息
     *
     * @return String 缓存统计信息
     */
    public static String getCacheStats() {
        return String.format(
                "Cache Stats - Inversion: %d, Numeric: %d, Decimal: %d",
                INVERSION_CACHE.size(), NUMERIC_CACHE.size(), DECIMAL_CACHE.size());
    }

    /**
     * 检查缓存是否需要清理
     */
    private static void checkCacheSize() {
        if (INVERSION_CACHE.size() > MAX_CACHE_SIZE) {
            // 清理最老的一半缓存项
            INVERSION_CACHE.clear();
        }
        if (NUMERIC_CACHE.size() > MAX_CACHE_SIZE) {
            NUMERIC_CACHE.clear();
        }
        if (DECIMAL_CACHE.size() > MAX_CACHE_SIZE) {
            DECIMAL_CACHE.clear();
        }
    }

    /**
     * 验证十六进制字符串格式
     *
     * @param hexString 十六进制字符串
     * @return boolean 是否为有效的十六进制字符串
     */
    public static boolean isValidHexString(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return false;
        }

        // 长度必须为偶数
        if (hexString.length() % 2 != 0) {
            return false;
        }

        // 检查每个字符是否为有效的十六进制字符
        for (int i = 0; i < hexString.length(); i++) {
            char c = hexString.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return String 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hexString 十六进制字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return new byte[0];
        }

        if (!isValidHexString(hexString)) {
            throw new IllegalArgumentException("Invalid hex string: " + hexString);
        }

        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)
                    ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private UnitKit() {
        // 私有构造函数，防止实例化
    }
}
