package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.core.FieldMetadata;
import com.lambda.cloud.netty.protocol.ProtocolException;

/**
 * 位数据转换器
 */
public class BitConverter implements DataTypeConverter {

    @Override
    public Object parse(byte[] data, FieldMetadata fieldMetadata) throws ProtocolException {
        try {
            StringBuilder result = new StringBuilder();

            for (byte b : data) {
                for (int i = 7; i >= 0; i--) {
                    result.append((b >> i) & 1);
                }
            }

            String bitString = result.toString();
            Class<?> fieldType = fieldMetadata.getFieldType();

            if (fieldType == String.class) {
                return bitString;
            } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                // 对于布尔类型，检查是否有任何位为1
                return bitString.contains("1");
            } else if (fieldType == Integer.class || fieldType == int.class) {
                return Integer.parseInt(bitString, 2);
            } else if (fieldType == Long.class || fieldType == long.class) {
                return Long.parseLong(bitString, 2);
            }
            return bitString;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "解析位数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public byte[] serialize(Object value, FieldMetadata fieldMetadata) throws ProtocolException {
        try {
            String bitString;

            if (value instanceof String) {
                bitString = (String) value;
                // 验证是否为有效的二进制字符串
                if (!bitString.matches("[01]+")) {
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.SERIALIZE_ERROR,
                            "位数据必须为二进制字符串: " + bitString,
                            fieldMetadata.getFieldName());
                }
            } else if (value instanceof Boolean) {
                bitString = (Boolean) value ? "1" : "0";
            } else if (value instanceof Number) {
                long longValue = ((Number) value).longValue();
                bitString = Long.toBinaryString(longValue);
            } else {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.SERIALIZE_ERROR,
                        "不支持的位数据类型: " + value.getClass().getName(),
                        fieldMetadata.getFieldName());
            }

            // 调整长度到字节边界
            int targetBits = fieldMetadata.getLength() * 8;
            if (bitString.length() < targetBits) {
                // 左填充0
                bitString = "0".repeat(targetBits - bitString.length()) + bitString;
            } else if (bitString.length() > targetBits) {
                // 截取右边部分
                bitString = bitString.substring(bitString.length() - targetBits);
            }

            // 转换为字节数组
            byte[] result = new byte[fieldMetadata.getLength()];
            for (int i = 0; i < result.length; i++) {
                int byteValue = 0;
                for (int j = 0; j < 8; j++) {
                    int bitIndex = i * 8 + j;
                    if (bitIndex < bitString.length() && bitString.charAt(bitIndex) == '1') {
                        byteValue |= (1 << (7 - j));
                    }
                }
                result[i] = (byte) byteValue;
            }

            return result;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化位数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, FieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return "0";
        }

        String trimmed = value.trim();
        if (!trimmed.matches("[01]+")) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "位字符串必须为二进制: " + trimmed, fieldMetadata.getFieldName());
        }

        Class<?> fieldType = fieldMetadata.getFieldType();
        if (fieldType == Boolean.class || fieldType == boolean.class) {
            return trimmed.contains("1");
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(trimmed, 2);
        } else if (fieldType == Long.class || fieldType == long.class) {
            return Long.parseLong(trimmed, 2);
        }
        return trimmed;
    }
}
