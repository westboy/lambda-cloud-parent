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
 *                          -- GETTER --
 *                          获取加密服务
 * @param delegateConverter 委托转换器（用于实际的数据类型转换）
 *                          -- GETTER --
 *                          获取委托转换器
 * @author Jin
 */
@Slf4j
public record EncryptedFieldConverter(EncryptionService encryptionService,
                                      DataTypeConverter delegateConverter) implements DataTypeConverter {

    /**
     * 构造函数
     *
     * @param encryptionService 加密服务
     * @param delegateConverter 委托转换器
     */
    public EncryptedFieldConverter(EncryptionService encryptionService, DataTypeConverter delegateConverter) {
        this.encryptionService = encryptionService;
        this.delegateConverter = delegateConverter;

        if (encryptionService == null) {
            throw new IllegalArgumentException("加密服务不能为空");
        }
        if (delegateConverter == null) {
            throw new IllegalArgumentException("委托转换器不能为空");
        }

        log.debug(
                "创建加密字段转换器，算法: {}, 委托转换器: {}",
                encryptionService.getAlgorithmName(),
                delegateConverter.getClass().getSimpleName());
    }

    @Override
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (data == null || data.length == 0) {
            return delegateConverter.parse(data, fieldMetadata);
        }

        try {
            // 检查是否为加密字段
            if (fieldMetadata.isEncrypted()) {
                if (log.isDebugEnabled()) {
                    log.debug("解析加密字段: {}, 加密数据长度: {}", fieldMetadata.getFieldName(), data.length);
                }

                // 先解密
                byte[] decryptedData = encryptionService.decrypt(data, fieldMetadata);

                if (log.isDebugEnabled()) {
                    log.debug("解密完成: {}, 解密后长度: {}", fieldMetadata.getFieldName(), decryptedData.length);
                }

                // 再解析
                return delegateConverter.parse(decryptedData, fieldMetadata);
            } else {
                // 非加密字段，直接解析
                return delegateConverter.parse(data, fieldMetadata);
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
            return delegateConverter.serialize(value, fieldMetadata);
        }

        try {
            // 先序列化
            byte[] serializedData = delegateConverter.serialize(value, fieldMetadata);

            // 检查是否为加密字段
            if (fieldMetadata.isEncrypted()) {
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
        // 字符串解析不涉及加密，直接委托
        return delegateConverter.parseFromString(value, fieldMetadata);
    }

    @Override
    public boolean supportsEncryption() {
        return true;
    }

    @Override
    public int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        if (fieldMetadata.isEncrypted()) {
            // 加密字段的长度可能会变化，返回配置的长度
            return fieldMetadata.getLength();
        } else {
            return delegateConverter.getExpectedLength(fieldMetadata);
        }
    }

    /**
     * 检查是否支持指定字段的加密
     *
     * @param fieldMetadata 字段元数据
     * @return true表示支持，false表示不支持
     */
    public boolean supportsField(ProtocolFieldMetadata fieldMetadata) {
        return fieldMetadata.isEncrypted() && encryptionService.supportsEncryption(fieldMetadata);
    }

    @Override
    public String toString() {
        return String.format(
                "EncryptedFieldConverter{algorithm=%s, delegate=%s}",
                encryptionService.getAlgorithmName(),
                delegateConverter.getClass().getSimpleName());
    }
}
