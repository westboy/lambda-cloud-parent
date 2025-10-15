package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.meta.ProtocolFieldMetadata;
import com.lambda.cloud.netty.utils.ConverterValidationUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 16位无符号整数转换器
 */
public class UInt16Converter implements DataTypeConverter {

    @Override
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        // 验证输入参数
        ConverterValidationUtils.validateBasicInputs(data, fieldMetadata, "UInt16");

        // 验证数据长度
        ConverterValidationUtils.validateDataLength(data, 2, fieldMetadata, "UInt16");

        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            if (fieldMetadata.isLittleEndian()) {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            } else {
                buffer.order(ByteOrder.BIG_ENDIAN);
            }

            int value = buffer.getShort() & 0xFFFF; // 转换为无符号

            // 验证范围
            ConverterValidationUtils.validateNumberRange(value, 0, 65535, fieldMetadata, "UInt16");

            Class<?> fieldType = fieldMetadata.getFieldType();
            if (ConverterValidationUtils.isIntegerType(fieldType)) {
                return value;
            } else if (ConverterValidationUtils.isShortType(fieldType)) {
                // 处理 Java 的有符号short类型
                return value > Short.MAX_VALUE ? (short) (value - 65536) : (short) value;
            } else if (fieldType == String.class) {
                return String.valueOf(value);
            }

            return value;
        } catch (Exception e) {
            throw ConverterValidationUtils.createParseException("解析UInt16数据失败: " + e.getMessage(), fieldMetadata, e);
        }
    }

    @Override
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        ConverterValidationUtils.validateSerializeValue(value, fieldMetadata, "UINT16");

        int intValue;

        if (value instanceof Number) {
            intValue = ((Number) value).intValue();
        } else if (value instanceof String) {
            intValue = Integer.parseUnsignedInt((String) value);
        } else {
            throw ConverterValidationUtils.createSerializeException(
                    "不支持的UINT16数据类型: " + value.getClass().getName(), fieldMetadata);
        }

        ConverterValidationUtils.validateNumberRange(intValue, 0, 0xFFFF, fieldMetadata, "UINT16");

        ByteBuffer buffer = ConverterValidationUtils.createByteBuffer(2, fieldMetadata);
        buffer.putShort((short) intValue);

        return buffer.array();
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }

        int intValue = Integer.parseUnsignedInt(value.trim());
        ConverterValidationUtils.validateNumberRange(intValue, 0, 0xFFFF, fieldMetadata, "UINT16");

        Class<?> fieldType = fieldMetadata.getFieldType();
        if (ConverterValidationUtils.isShortType(fieldType)) {
            return (short) intValue;
        }
        return intValue;
    }

    @Override
    public int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        return 2;
    }
}
