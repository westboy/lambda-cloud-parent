package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.utils.PrimitiveTypeUtils;
import com.lambda.cloud.netty.utils.ValidationUtils;
import io.netty.buffer.ByteBuf;

/**
 * 8位无符号整数转换器
 */
public class UInt8Converter implements DataTypeConverter {

    @Override
    public Object parse(ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        byte[] data = new byte[length];
        buffer.readBytes(data);

        ValidationUtils.validateBasicInputs(data, fieldMetadata, "UINT8");
        ValidationUtils.validateDataLength(data, 1, fieldMetadata, "UINT8");

        int unsignedValue = Byte.toUnsignedInt(data[0]);

        Class<?> fieldType = fieldMetadata.getFieldType();
        if (PrimitiveTypeUtils.isIntegerType(fieldType)) {
            return unsignedValue;
        } else if (PrimitiveTypeUtils.isByteType(fieldType)) {
            return data[0];
        } else if (fieldType == String.class) {
            return String.valueOf(unsignedValue);
        }
        return unsignedValue;
    }

    @Override
    public void serialize(Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        ValidationUtils.validateSerializeValue(value, fieldMetadata, "UINT8");
        int intValue;

        if (value instanceof Number) {
            intValue = ((Number) value).intValue();
        } else if (value instanceof String) {
            intValue = Integer.parseUnsignedInt((String) value);
        } else {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "不支持的UINT8数据类型: " + value.getClass().getName(),
                    fieldMetadata.getFieldName());
        }

        ValidationUtils.validateNumberRange(intValue, 0, 0xFF, fieldMetadata, "UINT8");

        buffer.writeByte((byte) intValue);
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            int intValue = Integer.parseUnsignedInt(value.trim());
            ValidationUtils.validateNumberRange(intValue, 0, 0xFF, fieldMetadata, "UINT8");

            Class<?> fieldType = fieldMetadata.getFieldType();
            if (PrimitiveTypeUtils.isByteType(fieldType)) {
                return (byte) intValue;
            }
            return intValue;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "从字符串解析UINT8数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        return 1;
    }
}
