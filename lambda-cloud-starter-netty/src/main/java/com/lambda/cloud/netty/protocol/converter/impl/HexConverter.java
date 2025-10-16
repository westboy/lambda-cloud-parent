package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import com.lambda.cloud.netty.utils.HexUtils;
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
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        validateLength(data, fieldMetadata);

        try {
            byte[] adjustedResult = adjustLength(data, fieldMetadata.getLength(), fieldMetadata.isLittleEndian());
            // 处理大小端字节序
            byte[] processedData = HexUtils.convertEndianness(adjustedResult, fieldMetadata.isLittleEndian());
            String hexString = HexUtils.bytesToHex(processedData);
            // 根据字段类型返回不同的对象

            int precision = fieldMetadata.getPrecision();

            BigInteger integerData = new BigInteger(hexString, 16);

            BigDecimal decimalValue = (precision > 0)
                    ? new BigDecimal(integerData).divide(BigDecimal.TEN.pow(precision), precision, RoundingMode.DOWN)
                    : new BigDecimal(integerData);

            Class<?> fieldType = fieldMetadata.getFieldType();

            if (fieldType == String.class) {
                return (precision > 0) ? decimalValue.stripTrailingZeros().toPlainString() : hexString;

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
            } else if (fieldType == byte[].class) {
                return data;
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

    @Override
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        try {
            byte[] result;
            int precision = fieldMetadata.getPrecision();

            switch (value) {
                case String hexString -> {
                    // 移除可能的空格和0x前缀
                    hexString = hexString.replaceAll("\\s+", "").replaceAll("^0x", "");
                    result = HexUtils.hexToBytes(hexString);
                }
                case Integer i -> {
                    int actualValue = precision > 0 ? (int) (i * Math.pow(10, precision)) : i;
                    String hexString = Integer.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtils.hexToBytes(hexString);
                }
                case Long l -> {
                    long actualValue = precision > 0 ? (long) (l * Math.pow(10, precision)) : l;
                    String hexString = Long.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtils.hexToBytes(hexString);
                }
                case Double d -> {
                    double actualValue = precision > 0 ? (d * Math.pow(10, precision)) : d.longValue();
                    String hexString = Double.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtils.hexToBytes(hexString);
                }
                case Float f -> {
                    float actualValue = precision > 0 ? (float) (f * Math.pow(10, precision)) : f.longValue();
                    String hexString = Float.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtils.hexToBytes(hexString);
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
                    result = HexUtils.hexToBytes(hexString);
                }
                case Byte b -> {
                    int actualValue = precision > 0 ? (int) (b * Math.pow(10, precision)) : b.intValue();
                    String hexString = Integer.toHexString(actualValue & 0xFF);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtils.hexToBytes(hexString);
                }
                case byte[] bytes -> result = bytes;
                default ->
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.SERIALIZE_ERROR,
                            "不支持的十六进制数据类型: " + value.getClass().getName(),
                            fieldMetadata.getFieldName());
            }
            // 处理大小端字节序
            var reversed = HexUtils.convertEndianness(result, fieldMetadata.isLittleEndian());

            // 调整长度（此时已经是正确的字节序）
            return adjustLength(reversed, fieldMetadata.getLength(), fieldMetadata.isLittleEndian());

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化十六进制数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // 移除空格和0x前缀
            String hexString = value.trim().replaceAll("\\s+", "").replaceAll("^0x", "");
            byte[] data = HexUtils.hexToBytes(hexString);
            return parse(data, fieldMetadata);
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
     * @param isLittleEndian 字段元数据
     * @return 调整后的数据
     */
    private byte[] adjustLength(byte[] data, int targetLength, boolean isLittleEndian) {
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
