package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

/**
 * 复合字段转换器
 * <p>
 * 支持复合对象的递归解析和序列化
 * </p>
 *
 * @author Jin
 */
@Slf4j
public record CompositeConverter(ReflectionProtocolEngine protocolEngine) implements DataTypeConverter {

    /**
     * 解析字节数据为复合对象
     *
     * @param data          字节数据
     * @param fieldMetadata 字段元数据
     * @return 解析后的复合对象
     * @throws ProtocolException 解析异常
     */
    @Override
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            // 获取复合字段的目标类型
            Class<?> targetType = fieldMetadata.getFieldType();

            // 验证目标类型
            validateTargetType(targetType, fieldMetadata);

            // 创建ByteBuf用于解析
            ByteBuf byteBuf = Unpooled.wrappedBuffer(data);

            // 使用协议引擎递归解析复合对象
            @SuppressWarnings("unchecked")
            Object compositeObject = protocolEngine.parse(byteBuf, (Class<Object>) targetType);

            log.debug("成功解析复合字段: {}, 类型: {}", fieldMetadata.getFieldName(), targetType.getSimpleName());

            return compositeObject;

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "复合字段解析失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    /**
     * 序列化复合对象为字节数据
     *
     * @param value         复合对象
     * @param fieldMetadata 字段元数据
     * @return 字节数据
     * @throws ProtocolException 序列化异常
     */
    @Override
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null) {
            return new byte[0];
        }
        // 创建ByteBuf用于序列化
        ByteBuf byteBuf = Unpooled.buffer();
        try {
            // 验证对象类型
            validateObjectType(value, fieldMetadata);

            // 使用协议引擎递归序列化复合对象
            protocolEngine.serialize(value, byteBuf);

            // 提取字节数据
            byte[] result = ByteBufUtil.getBytes(byteBuf);
            log.debug("成功序列化复合字段: {}, 长度: {}", fieldMetadata.getFieldName(), result.length);

            return result;

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "复合字段序列化失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        } finally {
            byteBuf.release();
        }
    }

    /**
     * 从字符串解析复合对象（暂不支持）
     *
     * @param data         字符串值
     * @param fieldMetadata 字段元数据
     * @return 解析后的对象
     * @throws ProtocolException 解析异常
     */
    @Override
    public Object parseFromString(String data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        // 复合字段不支持从字符串解析
        throw new ProtocolException(
                ProtocolException.ErrorCode.PARSE_ERROR,
                "复合字段不支持从字符串解析: " + fieldMetadata.getFieldName(),
                fieldMetadata.getFieldName());
    }

    /**
     * 验证数据长度（复合字段长度动态计算）
     *
     * @param data          字节数据
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 验证异常
     */
    @Override
    public void validateLength(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (data == null) {
            return;
        }

        // 复合字段的长度验证较为复杂，这里进行基本检查
        int expectedLength = fieldMetadata.getLength();
        if (expectedLength > 0 && data.length != expectedLength) {
            log.warn("复合字段长度不匹配: 期望={}, 实际={}, 字段={}", expectedLength, data.length, fieldMetadata.getFieldName());
        }
    }

    /**
     * 验证目标类型
     *
     * @param targetType    目标类型
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 验证异常
     */
    private void validateTargetType(Class<?> targetType, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (targetType == null) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.TYPE_MISMATCH,
                    "复合字段目标类型不能为null: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName());
        }

        // 检查是否可以实例化
        try {
            targetType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.TYPE_MISMATCH,
                    "复合字段目标类型缺少默认构造函数: " + targetType.getName(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    /**
     * 验证对象类型
     *
     * @param value         对象值
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 验证异常
     */
    private void validateObjectType(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        Class<?> expectedType = fieldMetadata.getFieldType();
        Class<?> actualType = value.getClass();

        if (!expectedType.isAssignableFrom(actualType)) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.TYPE_MISMATCH,
                    String.format(
                            "复合字段类型不匹配: 期望=%s, 实际=%s, 字段=%s",
                            expectedType.getName(), actualType.getName(), fieldMetadata.getFieldName()),
                    fieldMetadata.getFieldName());
        }
    }
}
