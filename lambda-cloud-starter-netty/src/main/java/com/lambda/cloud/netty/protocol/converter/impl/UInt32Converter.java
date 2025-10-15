package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.protocol.ProtocolException;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.core.FieldMetadata;
import com.lambda.cloud.netty.util.ConverterValidationUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 32位无符号整数转换器
 * <p>
 * 处理UINT32数据类型的转换
 * </p>
 *
 * @author Jin
 */
public class UInt32Converter implements DataTypeConverter {

    @Override
    public Object parse(byte[] data, FieldMetadata fieldMetadata) throws ProtocolException {
        if (data.length != 4) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "UINT32数据长度必须为4字节，实际: " + data.length,
                    fieldMetadata.getFieldName());
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(fieldMetadata.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

            int value = buffer.getInt();
            long unsignedValue = Integer.toUnsignedLong(value);

            // 根据字段类型返回不同的对象
            Class<?> fieldType = fieldMetadata.getFieldType();

            if (ConverterValidationUtils.isLongType(fieldType)) {
                return unsignedValue;
            } else if (ConverterValidationUtils.isIntegerType(fieldType)) {
                return value;
            } else if (fieldType == String.class) {
                return String.valueOf(unsignedValue);
            } else {
                return unsignedValue; // 默认返回长整型
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "解析UINT32数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public byte[] serialize(Object value, FieldMetadata fieldMetadata) throws ProtocolException {
        ConverterValidationUtils.validateSerializeValue(value, fieldMetadata, "UINT32");

        try {
            long longValue;

            if (value instanceof Number) {
                longValue = ((Number) value).longValue();
            } else if (value instanceof String) {
                longValue = Long.parseUnsignedLong((String) value);
            } else {
                throw ConverterValidationUtils.createSerializeException(
                        "不支持的UINT32数据类型: " + value.getClass().getName(), fieldMetadata);
            }

            // 检查范围
            ConverterValidationUtils.validateNumberRange(longValue, 0L, 0xFFFFFFFFL, fieldMetadata, "UINT32");

            ByteBuffer buffer = ConverterValidationUtils.createByteBuffer(4, fieldMetadata);
            buffer.putInt((int) longValue);

            return buffer.array();

        } catch (ProtocolException e) {
            throw e;
        } catch (Exception e) {
            throw ConverterValidationUtils.createSerializeException(
                    "序列化UINT32数据失败: " + e.getMessage(), fieldMetadata, e);
        }
    }

    @Override
    public Object parseFromString(String value, FieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }

        try {
            long longValue = Long.parseUnsignedLong(value.trim());

            // 检查范围
            ConverterValidationUtils.validateNumberRange(longValue, 0L, 0xFFFFFFFFL, fieldMetadata, "UINT32");

            // 根据字段类型返回
            Class<?> fieldType = fieldMetadata.getFieldType();
            if (ConverterValidationUtils.isIntegerType(fieldType)) {
                return (int) longValue;
            } else {
                return longValue;
            }
        } catch (ProtocolException e) {
            throw e;
        } catch (Exception e) {
            throw ConverterValidationUtils.createParseException(
                    "从字符串解析UINT32数据失败: " + e.getMessage(), fieldMetadata, e);
        }
    }

    @Override
    public int getExpectedLength(FieldMetadata fieldMetadata) {
        return 4;
    }
}
