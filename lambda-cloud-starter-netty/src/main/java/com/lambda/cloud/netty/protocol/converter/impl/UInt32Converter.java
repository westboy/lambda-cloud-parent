package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.utils.PrimitiveTypeUtils;
import com.lambda.cloud.netty.utils.ValidationUtils;
import io.netty.buffer.ByteBuf;
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
    public Object parse(ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        byte[] data = new byte[length];
        buffer.readBytes(data);
        if (data.length != 4) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "UINT32数据长度必须为4字节，实际: " + data.length,
                    fieldMetadata.getFieldName());
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        try {
            byteBuffer.order(fieldMetadata.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

            int value = byteBuffer.getInt();
            long unsignedValue = Integer.toUnsignedLong(value);

            // 根据字段类型返回不同的对象
            Class<?> fieldType = fieldMetadata.getFieldType();

            if (PrimitiveTypeUtils.isLongType(fieldType)) {
                return unsignedValue;
            } else if (PrimitiveTypeUtils.isIntegerType(fieldType)) {
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
        } finally {
            byteBuffer.clear();
        }
    }

    @Override
    public void serialize(Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        ValidationUtils.validateSerializeValue(value, fieldMetadata, "UINT32");

        try {
            long longValue;

            if (value instanceof Number) {
                longValue = ((Number) value).longValue();
            } else if (value instanceof String) {
                longValue = Long.parseUnsignedLong((String) value);
            } else {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.SERIALIZE_ERROR,
                        "不支持的UINT32数据类型: " + value.getClass().getName(),
                        fieldMetadata.getFieldName());
            }

            // 检查范围
            ValidationUtils.validateNumberRange(longValue, 0L, 0xFFFFFFFFL, fieldMetadata, "UINT32");

            if (fieldMetadata.isLittleEndian()) {
                buffer.writeIntLE((int) longValue);
            } else {
                buffer.writeInt((int) longValue);
            }

        } catch (ProtocolException e) {
            throw e;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化UINT32数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }

        try {
            long longValue = Long.parseUnsignedLong(value.trim());

            // 检查范围
            ValidationUtils.validateNumberRange(longValue, 0L, 0xFFFFFFFFL, fieldMetadata, "UINT32");

            // 根据字段类型返回
            Class<?> fieldType = fieldMetadata.getFieldType();
            if (PrimitiveTypeUtils.isIntegerType(fieldType)) {
                return (int) longValue;
            } else {
                return longValue;
            }
        } catch (ProtocolException e) {
            throw e;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "从字符串解析UINT32数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        return 4;
    }
}
