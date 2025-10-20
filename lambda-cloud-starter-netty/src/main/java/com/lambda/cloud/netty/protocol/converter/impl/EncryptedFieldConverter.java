package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.encryption.EncryptionService;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import lombok.extern.slf4j.Slf4j;

/**
 * 加密字段转换器
 * <p>
 * 专门处理加密字段的转换器，支持透明的加密解密操作
 * </p>
 *
 * @param encryptionService 加密服务
 *
 * @param nextConverter 下游类型转换器（用于实际的数据类型转换）
 *
 * @author Jin
 */
@Slf4j
public record EncryptedFieldConverter(
        EncryptionService encryptionService,
        // 下游类型转换器
        DataTypeConverter nextConverter)
        implements DataTypeConverter {

    /**
     * 构造函数
     *
     * @param encryptionService 加密服务
     * @param nextConverter 委托转换器
     */
    public EncryptedFieldConverter(EncryptionService encryptionService, DataTypeConverter nextConverter) {
        this.encryptionService = encryptionService;
        this.nextConverter = nextConverter;

        if (encryptionService == null) {
            throw new IllegalArgumentException("加密服务不能为空");
        }
        if (nextConverter == null) {
            throw new IllegalArgumentException("委托转换器不能为空");
        }

        log.debug(
                "创建加密字段转换器，算法: {}, 委托转换器: {}",
                encryptionService.getAlgorithmName(),
                nextConverter.getClass().getSimpleName());
    }

    @Override
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (data == null || data.length == 0) {
            return nextConverter.parse(data, fieldMetadata);
        }

        try {
            // 检查是否为加密字段
            if (fieldMetadata.isEncryptedField()) {
                if (log.isDebugEnabled()) {
                    log.debug("解析加密字段: {}, 加密数据长度: {}", fieldMetadata.getFieldName(), data.length);
                }

                // 先解密
                byte[] decryptedData = encryptionService.decrypt(data, fieldMetadata);

                if (log.isDebugEnabled()) {
                    log.debug("解密完成: {}, 解密后长度: {}", fieldMetadata.getFieldName(), decryptedData.length);
                }

                // 再解析
                return nextConverter.parse(decryptedData, fieldMetadata);
            } else {
                // 非加密字段，直接解析
                return nextConverter.parse(data, fieldMetadata);
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "加密字段解析失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null) {
            return nextConverter.serialize(null, fieldMetadata);
        }

        try {
            // 先序列化
            byte[] serializedData = nextConverter.serialize(value, fieldMetadata);

            // 检查是否为加密字段
            if (fieldMetadata.isEncryptedField()) {
                if (log.isDebugEnabled()) {
                    log.debug("序列化加密字段: {}, 原始数据长度: {}", fieldMetadata.getFieldName(), serializedData.length);
                }

                // 再加密
                byte[] encryptedData = encryptionService.encrypt(serializedData, fieldMetadata);

                if (log.isDebugEnabled()) {
                    log.debug("加密完成: {}, 加密后长度: {}", fieldMetadata.getFieldName(), encryptedData.length);
                }

                return encryptedData;
            } else {
                // 非加密字段，直接返回序列化结果
                return serializedData;
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "加密字段序列化失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        return nextConverter.parseFromString(value, fieldMetadata);
    }

    @Override
    public int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        if (fieldMetadata.isEncryptedField()) {
            return fieldMetadata.getLength();
        } else {
            return nextConverter.getExpectedLength(fieldMetadata);
        }
    }
}
