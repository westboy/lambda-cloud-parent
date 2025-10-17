package com.lambda.cloud.netty.protocol.processor;

import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterFactory;
import com.lambda.cloud.netty.protocol.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * CRC处理器辅助类
 * 提供字段值获取和序列化的辅助方法
 *
 * @author lambda
 * @since 2024-01-01
 */
public class CrcProcessorHelper {

    private static final DataTypeConverterFactory converterFactory = new DataTypeConverterFactory();

    /**
     * 获取字段值
     *
     * @param instance      消息实例
     * @param fieldMetadata 字段元数据
     * @return 字段值
     * @throws ProtocolException 获取失败
     */
    public static Object getFieldValue(Object instance, ProtocolFieldMetadata fieldMetadata) 
            throws ProtocolException {
        return fieldMetadata.getValue(instance);
    }

    /**
     * 设置字段值
     *
     * @param instance      消息实例
     * @param fieldMetadata 字段元数据
     * @param value         字段值
     * @throws ProtocolException 设置失败
     */
    public static void setFieldValue(Object instance, ProtocolFieldMetadata fieldMetadata, Object value) 
            throws ProtocolException {
        fieldMetadata.setValue(instance, value);
    }

    /**
     * 序列化字段值到ByteBuf
     *
     * @param byteBuf       字节缓冲区
     * @param fieldValue    字段值
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 序列化失败
     */
    public static void serializeFieldValue(ByteBuf byteBuf, Object fieldValue, 
                                         ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        try {
            DataTypeConverter converter = converterFactory.getConverter(fieldMetadata.getDataType());
            byte[] serializedData = converter.serialize(fieldValue, fieldMetadata);
            byteBuf.writeBytes(serializedData);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化字段失败: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    /**
     * 确定CRC算法名称
     *
     * @param crcField CRC字段元数据
     * @return CRC算法名称
     */
    public static String determineCrcAlgorithm(ProtocolFieldMetadata crcField) {
        // 根据字段长度确定默认算法
        int length = crcField.getLength();
        if (length == 2) {
            return "CRC16";
        } else if (length == 4) {
            return "CRC32";
        } else {
            return "CRC16"; // 默认使用CRC16
        }
    }
}