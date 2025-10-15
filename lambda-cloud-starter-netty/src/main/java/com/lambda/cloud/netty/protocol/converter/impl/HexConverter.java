package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.protocol.ProtocolException;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.core.FieldMetadata;
import com.lambda.cloud.netty.util.HexUtils;

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
    public Object parse(byte[] data, FieldMetadata fieldMetadata) throws ProtocolException {
        validateLength(data, fieldMetadata);

        try {
            String hexString = HexUtils.bytesToHex(data);
            // 根据字段类型返回不同的对象
            Class<?> fieldType = fieldMetadata.getFieldType();
            int precision = fieldMetadata.getPrecision();

            if (fieldType == String.class) {
                return hexString;
            } else if (fieldType == Integer.class || fieldType == int.class) {
                long value = Long.parseLong(hexString, 16);
                if (precision > 0) {
                    return (int) (value / Math.pow(10, precision));
                }
                return (int) value;
            } else if (fieldType == Long.class || fieldType == long.class) {
                long value = Long.parseLong(hexString, 16);
                if (precision > 0) {
                    return (long) (value / Math.pow(10, precision));
                }
                return value;
            } else if (fieldType == Double.class || fieldType == double.class) {
                long value = Long.parseLong(hexString, 16);
                if (precision > 0) {
                    return value / Math.pow(10, precision);
                }
                return (double) value;
            } else if (fieldType == Float.class || fieldType == float.class) {
                long value = Long.parseLong(hexString, 16);
                if (precision > 0) {
                    return (float) (value / Math.pow(10, precision));
                }
                return (float) value;
            } else if (fieldType == byte[].class) {
                return data;
            } else {
                return hexString; // 默认返回字符串
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
    public byte[] serialize(Object value, FieldMetadata fieldMetadata) throws ProtocolException {
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
                    long actualValue = precision > 0 ? (long) (i * Math.pow(10, precision)) : i;
                    String hexString = Long.toHexString(actualValue);
                    // 确保长度为偶数
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
                    long actualValue = precision > 0 ? (long) (d * Math.pow(10, precision)) : d.longValue();
                    String hexString = Long.toHexString(actualValue);
                    if (hexString.length() % 2 != 0) {
                        hexString = "0" + hexString;
                    }
                    result = HexUtils.hexToBytes(hexString);
                }
                case Float f -> {
                    long actualValue = precision > 0 ? (long) (f * Math.pow(10, precision)) : f.longValue();
                    String hexString = Long.toHexString(actualValue);
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

            // 调整长度
            return adjustLength(result, fieldMetadata.getLength(), fieldMetadata);

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化十六进制数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, FieldMetadata fieldMetadata) throws ProtocolException {
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
     * @param fieldMetadata 字段元数据
     * @return 调整后的数据
     */
    private byte[] adjustLength(byte[] data, int targetLength, FieldMetadata fieldMetadata) {
        if (data.length == targetLength) {
            return data;
        }

        byte[] result = new byte[targetLength];

        if (data.length < targetLength) {
            // 数据不足，根据字节序填充
            if (fieldMetadata.isLittleEndian()) {
                // 小端序：数据在低位，高位填充0
                System.arraycopy(data, 0, result, 0, data.length);
            } else {
                // 大端序：数据在高位，低位填充0
                System.arraycopy(data, 0, result, targetLength - data.length, data.length);
            }
        } else {
            // 数据过长，截取
            if (fieldMetadata.isLittleEndian()) {
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
