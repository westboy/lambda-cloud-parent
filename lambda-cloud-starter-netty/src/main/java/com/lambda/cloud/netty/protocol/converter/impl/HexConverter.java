package com.lambda.cloud.netty.protocol.converter.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.annotation.PaddingDirection;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.utils.ValidationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * 十六进制数据转换器
 * <p>
 * 处理十六进制格式的数据转换
 * </p>
 *
 * @author Jin
 */
public class HexConverter implements DataTypeConverter {

    @Override
    public Object parse(ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        byte[] data = new byte[length];
        buffer.readBytes(data);

        // 基本输入验证
        ValidationUtils.validateBasicInputs(data, fieldMetadata, "十六进制");

        try {
            // 修正：先处理字节序，再调整长度（避免双重字节序处理）
            byte[] processedData = convertEndianness(data, fieldMetadata.isLittleEndian());
            byte[] adjustedResult = fieldMetadata.getLength() > 0
                    ? adjustLength(processedData, fieldMetadata.getLength(), false)
                    : processedData;
            String hexString = HexUtil.encodeHexStr(adjustedResult);
            // 根据字段类型返回不同的对象

            Class<?> fieldType = fieldMetadata.getFieldType();

            if (fieldType == byte[].class) {
                return data;
            }

            int precision = fieldMetadata.getPrecision();

            // 如果非 byte[] 类型且数据为空，直接返回默认值或抛异常，避免 Zero length BigInteger
            if (hexString.isEmpty()) {
                if (fieldType == String.class) return "";
                if (fieldType == Integer.class || fieldType == int.class) return 0;
                if (fieldType == Long.class || fieldType == long.class) return 0L;
                if (fieldType == Byte.class || fieldType == byte.class) return (byte) 0;
                if (fieldType == Double.class || fieldType == double.class) return 0.0d;
                if (fieldType == Float.class || fieldType == float.class) return 0.0f;
                if (fieldType == BigDecimal.class) return BigDecimal.ZERO;
                return "0";
            }

            BigInteger integerData = new BigInteger(hexString, 16);

            // 修正：使用BigDecimal.TEN.pow避免精度损失
            BigDecimal decimalValue = (precision > 0)
                    ? new BigDecimal(integerData).divide(BigDecimal.TEN.pow(precision), precision, RoundingMode.DOWN)
                    : new BigDecimal(integerData);

            if (fieldType == String.class) {
                if (precision > 0) {
                    return decimalValue.stripTrailingZeros().toPlainString();
                }
                if ("frameType".equalsIgnoreCase(fieldMetadata.getFieldName())) {
                    return hexString.toUpperCase();
                }
                return normalizeHexStringForStringField(hexString, fieldMetadata);

            } else if (fieldType == Integer.class || fieldType == int.class) {
                return decimalValue.intValue();

            } else if (fieldType == Long.class || fieldType == long.class) {
                return decimalValue.longValue();

            } else if (fieldType == Byte.class || fieldType == byte.class) {
                return decimalValue.byteValue();

            } else if (fieldType == Double.class || fieldType == double.class) {
                return decimalValue.doubleValue();

            } else if (fieldType == Float.class || fieldType == float.class) {
                return decimalValue.floatValue();

            } else if (fieldType == BigDecimal.class) {
                return decimalValue;
            } else {
                return decimalValue.stripTrailingZeros().toPlainString();
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "解析十六进制数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @SuppressWarnings("all")
    @Override
    public void serialize(Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        // 基本输入验证
        ValidationUtils.validateSerializeValue(value, fieldMetadata, "十六进制");

        try {
            byte[] result;
            int precision = fieldMetadata.getPrecision();

            switch (value) {
                case String str -> {
                    // 移除可能的空格和0x前缀
                    str = str.replaceAll("\\s+", "").replaceAll("^0x", "");
                    if (HexUtil.isHexNumber(str)) {
                        if (str.length() % 2 != 0) {
                            str = "0" + str;
                        }
                        result = HexUtil.decodeHex(str);
                    } else {
                        String hexStr = HexUtil.encodeHexStr(str);
                        result = HexUtil.decodeHex(hexStr);
                    }
                }
                case Integer i -> {
                    // 修正：使用BigDecimal避免精度损失
                    long actualValue = precision > 0
                            ? new BigDecimal(i)
                                    .multiply(BigDecimal.TEN.pow(precision))
                                    .longValue()
                            : i;
                    String hexString = Long.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtil.decodeHex(hexString);
                }
                case Long l -> {
                    // 修正：使用BigDecimal避免精度损失
                    long actualValue = precision > 0
                            ? new BigDecimal(l)
                                    .multiply(BigDecimal.TEN.pow(precision))
                                    .longValue()
                            : l;
                    String hexString = Long.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtil.decodeHex(hexString);
                }
                case Double d -> {
                    // 修正：Double转换为long后再转十六进制，避免IEEE 754格式
                    long actualValue = precision > 0
                            ? new BigDecimal(d)
                                    .multiply(BigDecimal.TEN.pow(precision))
                                    .longValue()
                            : d.longValue();
                    String hexString = Long.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtil.decodeHex(hexString);
                }
                case Float f -> {
                    // 修正：Float转换为long后再转十六进制，避免IEEE 754格式
                    long actualValue = precision > 0
                            ? new BigDecimal(f)
                                    .multiply(BigDecimal.TEN.pow(precision))
                                    .longValue()
                            : f.longValue();
                    String hexString = Long.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtil.decodeHex(hexString);
                }
                case BigDecimal b -> {
                    // 根据精度将BigDecimal转换为整数值
                    BigDecimal scaledValue = precision > 0 ? b.multiply(BigDecimal.TEN.pow(precision)) : b;

                    // 转换为BigInteger（去除小数部分）
                    BigInteger integerValue = scaledValue.toBigInteger();

                    // 转换为十六进制字符串
                    String hexString = integerValue.toString(16);

                    // 确保十六进制字符串长度为偶数
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }

                    // 转换为字节数组（大端序格式）
                    result = HexUtil.decodeHex(hexString);
                }
                case Byte b -> {
                    // 修正：使用BigDecimal避免精度损失
                    int actualValue = precision > 0
                            ? new BigDecimal(b)
                                    .multiply(BigDecimal.TEN.pow(precision))
                                    .intValue()
                            : b.intValue();
                    String hexString = Integer.toHexString(actualValue & 0xFF);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtil.decodeHex(hexString);
                }
                case byte[] bytes -> result = bytes;
                default ->
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.SERIALIZE_ERROR,
                            "不支持的十六进制数据类型: " + value.getClass().getName(),
                            fieldMetadata.getFieldName());
            }

            // 修正：先调整长度，再处理字节序（避免双重字节序处理）
            byte[] adjustedResult = adjustLength(result, fieldMetadata.getLength(), false); // 传false避免内部字节序处理
            byte[] finalResult = convertEndianness(adjustedResult, fieldMetadata.isLittleEndian());
            buffer.writeBytes(finalResult);

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化十六进制数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    public static byte[] convertEndianness(byte[] bytes, boolean littleEndian) {
        if (littleEndian) {
            return ArrayUtil.reverse(bytes);
        }
        return bytes;
    }

    private static String normalizeHexStringForStringField(String hexString, ProtocolFieldMetadata fieldMetadata) {
        if (hexString == null || hexString.isEmpty()) {
            return hexString;
        }
        if (fieldMetadata.getPaddingDirection() != PaddingDirection.NONE) {
            return hexString;
        }
        if (!"0".equals(fieldMetadata.getPaddingChar())) {
            return hexString;
        }
        int firstNonZeroIndex = 0;
        while (firstNonZeroIndex < hexString.length() && hexString.charAt(firstNonZeroIndex) == '0') {
            firstNonZeroIndex++;
        }
        if (firstNonZeroIndex == 0 || firstNonZeroIndex == hexString.length()) {
            return hexString;
        }
        return hexString.substring(firstNonZeroIndex);
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        // 早期返回优化
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // 优化：使用StringBuilder避免多次字符串操作
            String trimmed = value.trim();
            if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
                trimmed = trimmed.substring(2);
            }
            String hexString = trimmed.replaceAll("\\s+", "");

            byte[] data = HexUtil.decodeHex(hexString);
            ByteBuf buffer = Unpooled.wrappedBuffer(data);
            try {
                return parse(buffer, data.length, fieldMetadata);
            } finally {
                buffer.release();
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "从字符串解析十六进制数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    /**
     * 调整字节数组长度
     *
     * @param data          原始数据
     * @param targetLength  目标长度
     * @param isLittleEndian 是否小端序（用于确定填充/截取方向）
     * @return 调整后的数据
     */
    private byte[] adjustLength(byte[] data, int targetLength, boolean isLittleEndian) {
        // 性能优化：早期返回避免不必要的数组复制
        if (data.length == targetLength) {
            return data;
        }

        byte[] result = new byte[targetLength];

        if (data.length < targetLength) {
            // 数据不足，根据字节序填充
            if (isLittleEndian) {
                // 小端序：数据在低位，高位填充0
                System.arraycopy(data, 0, result, 0, data.length);
            } else {
                // 大端序：数据在高位，低位填充0
                System.arraycopy(data, 0, result, targetLength - data.length, data.length);
            }
        } else {
            // 数据过长，截取
            if (isLittleEndian) {
                // 小端序：取低位数据
                System.arraycopy(data, 0, result, 0, targetLength);
            } else {
                // 大端序：取高位数据
                System.arraycopy(data, data.length - targetLength, result, 0, targetLength);
            }
        }

        return result;
    }
}
