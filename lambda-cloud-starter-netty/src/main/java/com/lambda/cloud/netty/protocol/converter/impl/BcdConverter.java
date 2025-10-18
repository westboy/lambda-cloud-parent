package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import com.lambda.cloud.netty.utils.ExceptionUtils;
import com.lambda.cloud.netty.utils.PrimitiveTypeUtils;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * BCD数据转换器
 */
public class BcdConverter implements DataTypeConverter {

    @Override
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        // 验证输入参数
        ValidationUtils.validateBasicInputs(data, fieldMetadata, "BCD");

        if (data.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;

            // 验证BCD数字有效性
            if (high > 9 || low > 9) {
                throw ExceptionUtils.createParseException("无效的BCD数字: " + String.format("0x%02X", b), fieldMetadata);
            }

            sb.append(high).append(low);
        }

        String result = sb.toString();

        // 移除前导零（但保留至少一个数字）
        result = result.replaceFirst("^0+(?!$)", "");

        Class<?> fieldType = fieldMetadata.getFieldType();
        try {
            if (PrimitiveTypeUtils.isLongType(fieldType)) {
                long value = Long.parseLong(result);
                // 验证长整型范围
                ValidationUtils.validateNumberRange(value, 0, Long.MAX_VALUE, fieldMetadata, "BCD长整型");
                return value;
            } else if (PrimitiveTypeUtils.isIntegerType(fieldType)) {
                int value = Integer.parseInt(result);
                // 验证整型范围
                ValidationUtils.validateNumberRange(value, 0, Integer.MAX_VALUE, fieldMetadata, "BCD整型");
                return value;
            }
        } catch (NumberFormatException e) {
            throw ExceptionUtils.createParseException("BCD字符串转换为数字失败: " + result, fieldMetadata, e);
        }
        return result;
    }

    @Override
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        try {
            String bcdString = value.toString();

            // 验证是否为数字
            if (!bcdString.matches("\\d+")) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.SERIALIZE_ERROR,
                        "BCD数据必须为数字: " + bcdString,
                        fieldMetadata.getFieldName());
            }

            // 确保长度为偶数
            if (bcdString.length() % 2 != 0) {
                bcdString = "0" + bcdString;
            }

            byte[] result = new byte[bcdString.length() / 2];

            for (int i = 0; i < result.length; i++) {
                int high = Character.getNumericValue(bcdString.charAt(i * 2));
                int low = Character.getNumericValue(bcdString.charAt(i * 2 + 1));
                result[i] = (byte) ((high << 4) | low);
            }

            // 调整长度
            if (result.length != fieldMetadata.getLength()) {
                byte[] adjusted = new byte[fieldMetadata.getLength()];
                if (result.length < adjusted.length) {
                    // 左填充0
                    System.arraycopy(result, 0, adjusted, adjusted.length - result.length, result.length);
                } else {
                    // 截取右边部分
                    System.arraycopy(result, result.length - adjusted.length, adjusted, 0, adjusted.length);
                }
                result = adjusted;
            }

            return result;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化BCD数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return "0";
        }

        String trimmed = value.trim();
        if (!trimmed.matches("\\d+")) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "BCD字符串必须为数字: " + trimmed, fieldMetadata.getFieldName());
        }

        Class<?> fieldType = fieldMetadata.getFieldType();
        if (fieldType == Long.class || fieldType == long.class) {
            return Long.parseLong(trimmed);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(trimmed);
        }
        return trimmed;
    }
}
