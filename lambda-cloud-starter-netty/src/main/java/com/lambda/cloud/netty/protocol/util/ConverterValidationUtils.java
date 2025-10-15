package com.lambda.cloud.netty.protocol.util;

import com.lambda.cloud.netty.protocol.core.FieldMetadata;
import com.lambda.cloud.netty.protocol.core.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 转换器验证工具类
 * <p>
 * 提供通用的验证方法，消除重复的验证逻辑
 * </p>
 *
 * @author Jin
 */
public class ConverterValidationUtils {

    /**
     * 验证数据和字段元数据的基本有效性
     *
     * @param data          数据
     * @param fieldMetadata 字段元数据
     * @param dataTypeName  数据类型名称（用于错误消息）
     * @throws ProtocolException 验证失败时抛出
     */
    public static void validateBasicInputs(byte[] data, FieldMetadata fieldMetadata, String dataTypeName)
            throws ProtocolException {
        if (fieldMetadata == null) {
            throw new ProtocolException(ProtocolException.ErrorCode.PARSE_ERROR, "字段元数据不能为null", null);
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
            byte[] data, int expectedLength, FieldMetadata fieldMetadata, String dataTypeName)
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
            long value, long minValue, long maxValue, FieldMetadata fieldMetadata, String dataTypeName)
            throws ProtocolException {
        if (value < minValue || value > maxValue) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    dataTypeName + "值超出范围[" + minValue + ", " + maxValue + "]: " + value,
                    fieldMetadata.getFieldName());
        }
    }

    /**
     * 创建解析错误异常
     *
     * @param message       错误消息
     * @param fieldMetadata 字段元数据
     * @param cause         原因异常
     * @return ProtocolException实例
     */
    public static ProtocolException createParseException(String message, FieldMetadata fieldMetadata, Throwable cause) {
        return new ProtocolException(
                ProtocolException.ErrorCode.PARSE_ERROR,
                message,
                fieldMetadata != null ? fieldMetadata.getFieldName() : null,
                cause);
    }

    /**
     * 创建解析错误异常
     *
     * @param message       错误消息
     * @param fieldMetadata 字段元数据
     * @return ProtocolException实例
     */
    public static ProtocolException createParseException(String message, FieldMetadata fieldMetadata) {
        return createParseException(message, fieldMetadata, null);
    }

    /**
     * 创建序列化错误异常
     *
     * @param message       错误消息
     * @param fieldMetadata 字段元数据
     * @param cause         原因异常
     * @return ProtocolException实例
     */
    public static ProtocolException createSerializeException(
            String message, FieldMetadata fieldMetadata, Throwable cause) {
        return new ProtocolException(
                ProtocolException.ErrorCode.SERIALIZE_ERROR,
                message,
                fieldMetadata != null ? fieldMetadata.getFieldName() : null,
                cause);
    }

    /**
     * 创建序列化错误异常
     *
     * @param message       错误消息
     * @param fieldMetadata 字段元数据
     * @return ProtocolException实例
     */
    public static ProtocolException createSerializeException(String message, FieldMetadata fieldMetadata) {
        return createSerializeException(message, fieldMetadata, null);
    }

    /**
     * 验证序列化值的基本有效性
     *
     * @param value         要序列化的值
     * @param fieldMetadata 字段元数据
     * @param dataTypeName  数据类型名称（用于错误消息）
     * @throws ProtocolException 验证失败时抛出
     */
    public static void validateSerializeValue(Object value, FieldMetadata fieldMetadata, String dataTypeName)
            throws ProtocolException {
        if (fieldMetadata == null) {
            throw createSerializeException("字段元数据不能为null", null);
        }

        if (value == null) {
            throw createSerializeException(dataTypeName + "序列化值不能为null", fieldMetadata);
        }
    }

    /**
     * 创建配置了字节序的ByteBuffer
     *
     * @param capacity      缓冲区容量
     * @param fieldMetadata 字段元数据
     * @return 配置了字节序的ByteBuffer
     */
    public static ByteBuffer createByteBuffer(int capacity, FieldMetadata fieldMetadata) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(fieldMetadata.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return buffer;
    }

    /**
     * 检查字段类型是否为整型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为整型
     */
    public static boolean isIntegerType(Class<?> fieldType) {
        return fieldType == Integer.class || fieldType == int.class;
    }

    /**
     * 检查字段类型是否为长整型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为长整型
     */
    public static boolean isLongType(Class<?> fieldType) {
        return fieldType == Long.class || fieldType == long.class;
    }

    /**
     * 检查字段类型是否为短整型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为短整型
     */
    public static boolean isShortType(Class<?> fieldType) {
        return fieldType == Short.class || fieldType == short.class;
    }

    /**
     * 检查字段类型是否为字节型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为字节型
     */
    public static boolean isByteType(Class<?> fieldType) {
        return fieldType == Byte.class || fieldType == byte.class;
    }

    /**
     * 检查字段类型是否为布尔型（包括包装类和基本类型）
     *
     * @param fieldType 字段类型
     * @return 是否为布尔型
     */
    public static boolean isBooleanType(Class<?> fieldType) {
        return fieldType == Boolean.class || fieldType == boolean.class;
    }
}
