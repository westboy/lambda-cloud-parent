package com.lambda.cloud.netty.protocol.converter;

import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;

/**
 * 数据类型转换器接口
 * <p>
 * 定义协议数据类型转换的统一标准
 * </p>
 *
 * @author Jin
 */
public interface DataTypeConverter {

    /**
     * 解析字节数据为对象
     *
     * @param data          字节数据
     * @param fieldMetadata 字段元数据
     * @return 解析后的对象
     * @throws ProtocolException 解析异常
     */
    Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException;

    /**
     * 序列化对象为字节数据
     *
     * @param value         对象值
     * @param fieldMetadata 字段元数据
     * @return 字节数据
     * @throws ProtocolException 序列化异常
     */
    byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException;

    /**
     * 从字符串解析对象（用于默认值）
     *
     * @param value         字符串值
     * @param fieldMetadata 字段元数据
     * @return 解析后的对象
     * @throws ProtocolException 解析异常
     */
    Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException;

    /**
     * 验证数据长度
     *
     * @param data          字节数据
     * @param fieldMetadata 字段元数据
     */
    default void validateLength(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        Assert.isTrue(
                data.length == fieldMetadata.getLength(),
                "ASCII数据长度不匹配，期望: " + fieldMetadata.getLength() + ", 实际: " + data.length);
    }

    /**
     * 获取期望的数据长度
     *
     * @param fieldMetadata 字段元数据
     * @return 数据长度
     */
    default int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        return fieldMetadata.getLength();
    }
}
