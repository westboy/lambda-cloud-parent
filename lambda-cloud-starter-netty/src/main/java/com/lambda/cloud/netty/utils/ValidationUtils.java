package com.lambda.cloud.netty.utils;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import java.util.regex.Pattern;

/**
 * 验证工具类
 * <p>
 * 提供常用的数据验证方法
 * </p>
 *
 * @author Jin
 */
public final class ValidationUtils {

    // 常用正则表达式模式
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9A-Fa-f]+$");
    private static final Pattern BCD_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern IP_PATTERN =
            Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern MAC_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");

    /**
     * 获取数据类型的默认长度
     *
     * @param dataType 数据类型
     * @return 默认长度
     */
    public static int getDefaultElementLength(ProtocolDataType dataType) {
        return switch (dataType) {
            case UINT8 -> 1;
            case UINT16 -> 2;
            case UINT32 -> 4;
            case UINT64 -> 8;
            case CP56TIME2A -> 7;
            default -> 0; // 变长类型
        };
    }

    /**
     * 是否全是0
     * @param data 数组
     * @return boolean
     */
    public static boolean isAllZero(byte[] data) {
        for (byte b : data) {
            if (b != 0) return false;
        }
        return true;
    }

    /**
     * 验证数值范围
     *
     * @param value 数值
     * @param min 最小值
     * @param max 最大值
     * @return true 表示在范围内
     */
    public static boolean isInRange(Number value, double min, double max) {
        if (value == null) {
            return false;
        }
        double doubleValue = value.doubleValue();
        return doubleValue >= min && doubleValue <= max;
    }

    /**
     * 验证字符串长度范围
     *
     * @param value 字符串
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return true 表示长度在范围内
     */
    public static boolean isLengthInRange(String value, int minLength, int maxLength) {
        if (value == null) {
            return minLength <= 0;
        }
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 验证是否为纯数字
     *
     * @param value 字符串
     * @return true 表示为纯数字
     */
    public static boolean isNumeric(String value) {
        return value != null && NUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * 验证是否为字母数字组合
     *
     * @param value 字符串
     * @return true 表示为字母数字组合
     */
    public static boolean isAlphanumeric(String value) {
        return value != null && ALPHANUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * 验证是否为有效的十六进制字符串
     *
     * @param value 字符串
     * @return true 表示为有效的十六进制
     */
    public static boolean isHex(String value) {
        return value != null && HEX_PATTERN.matcher(value).matches();
    }

    /**
     * 验证是否为有效的BCD编码
     *
     * @param value 字符串
     * @return true 表示为有效的BCD编码
     */
    public static boolean isBcd(String value) {
        return value != null && BCD_PATTERN.matcher(value).matches();
    }

    /**
     * 验证是否为有效的IP地址
     *
     * @param value 字符串
     * @return true 表示为有效的IP地址
     */
    public static boolean isValidIp(String value) {
        return value != null && IP_PATTERN.matcher(value).matches();
    }

    /**
     * 验证是否为有效的MAC地址
     *
     * @param value 字符串
     * @return true 表示为有效的MAC地址
     */
    public static boolean isValidMac(String value) {
        return value != null && MAC_PATTERN.matcher(value).matches();
    }

    /**
     * 验证整数是否为正数
     *
     * @param value 整数
     * @return true 表示为正数
     */
    public static boolean isPositive(Number value) {
        return value != null && value.doubleValue() > 0;
    }

    /**
     * 验证整数是否为非负数
     *
     * @param value 整数
     * @return true 表示为非负数
     */
    public static boolean isNonNegative(Number value) {
        return value != null && value.doubleValue() >= 0;
    }

    /**
     * 验证浮点数精度
     *
     * @param value 浮点数
     * @param precision 精度（小数位数）
     * @return true 表示精度符合要求
     */
    public static boolean checkPrecision(Double value, int precision) {
        if (value == null) {
            return false;
        }
        String str = value.toString();
        int dotIndex = str.indexOf('.');
        if (dotIndex == -1) {
            return precision >= 0;
        }
        int actualPrecision = str.length() - dotIndex - 1;
        return actualPrecision <= precision;
    }

    /**
     * 验证时间戳是否在合理范围内
     *
     * @param timestamp 时间戳（毫秒）
     * @return true 表示在合理范围内
     */
    public static boolean isValidTimestamp(Long timestamp) {
        if (timestamp == null) {
            return false;
        }
        // 检查时间戳是否在1970年到2100年之间
        long min = 0L; // 1970-01-01
        long max = 4102444800000L; // 2100-01-01
        return timestamp >= min && timestamp <= max;
    }

    /**
     * 验证字符串是否包含非法字符
     *
     * @param value 字符串
     * @param illegalChars 非法字符数组
     * @return true 表示不包含非法字符
     */
    public static boolean doesNotContainIllegalChars(String value, char[] illegalChars) {
        if (value == null || illegalChars == null) {
            return true;
        }
        for (char c : value.toCharArray()) {
            for (char illegal : illegalChars) {
                if (c == illegal) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 验证字符串是否只包含指定字符集
     *
     * @param value 字符串
     * @param allowedChars 允许的字符集
     * @return true 表示只包含允许的字符
     */
    public static boolean containsOnlyAllowedChars(String value, String allowedChars) {
        if (value == null) {
            return true;
        }
        if (allowedChars == null) {
            return false;
        }
        for (char c : value.toCharArray()) {
            if (allowedChars.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证数据和字段元数据的基本有效性
     *
     * @param data          数据
     * @param fieldMetadata 字段元数据
     * @param dataTypeName  数据类型名称（用于错误消息）
     * @throws ProtocolException 验证失败时抛出
     */
    public static void validateBasicInputs(byte[] data, ProtocolFieldMetadata fieldMetadata, String dataTypeName)
            throws ProtocolException {
        if (fieldMetadata == null) {
            throw new ProtocolException(ProtocolException.ErrorCode.PARSE_ERROR, "字段元数据不能为null");
        }

        if (data == null) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, dataTypeName + "数据不能为null", fieldMetadata.getFieldName());
        }
    }

    /**
     * 验证数据长度
     *
     * @param data           数据
     * @param expectedLength 期望长度
     * @param fieldMetadata  字段元数据
     * @param dataTypeName   数据类型名称（用于错误消息）
     * @throws ProtocolException 验证失败时抛出
     */
    public static void validateDataLength(
            byte[] data, int expectedLength, ProtocolFieldMetadata fieldMetadata, String dataTypeName)
            throws ProtocolException {
        if (data.length != expectedLength) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    dataTypeName + "数据长度必须为" + expectedLength + "字节，实际: " + data.length,
                    fieldMetadata.getFieldName());
        }
    }

    /**
     * 验证数值范围
     *
     * @param value         数值
     * @param minValue      最小值
     * @param maxValue      最大值
     * @param fieldMetadata 字段元数据
     * @param dataTypeName  数据类型名称（用于错误消息）
     * @throws ProtocolException 验证失败时抛出
     */
    public static void validateNumberRange(
            long value, long minValue, long maxValue, ProtocolFieldMetadata fieldMetadata, String dataTypeName)
            throws ProtocolException {
        if (value < minValue || value > maxValue) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    dataTypeName + "值超出范围: " + value + " (范围: " + minValue + "-" + maxValue + ")",
                    fieldMetadata != null ? fieldMetadata.getFieldName() : null);
        }
    }

    /**
     * 验证序列化值的基本有效性
     *
     * @param value         要序列化的值
     * @param fieldMetadata 字段元数据
     * @param dataTypeName  数据类型名称（用于错误消息）
     * @throws ProtocolException 验证失败时抛出
     */
    public static void validateSerializeValue(Object value, ProtocolFieldMetadata fieldMetadata, String dataTypeName)
            throws ProtocolException {
        if (fieldMetadata == null) {
            throw new ProtocolException(ProtocolException.ErrorCode.SERIALIZE_ERROR, "字段元数据不能为null", (String) null);
        }

        if (value == null) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    dataTypeName + "序列化值不能为null",
                    fieldMetadata.getFieldName());
        }
    }
}
