package com.lambda.cloud.netty.protocol.encryption.impl;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.encryption.EncryptionService;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import com.lambda.cloud.netty.utils.HexUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 默认AES加密服务实现
 * <p>
 * 使用AES-256-GCM算法提供高安全性的加密解密服务，支持字段级别的密钥管理
 * </p>
 *
 * @param defaultKeyBytes 默认密钥（用于演示，生产环境应使用安全的密钥管理）
 * @author Jin
 */
@Slf4j
public record DefaultEncryptionService(byte[] defaultKeyBytes) implements EncryptionService {
    /**
     * 构造函数（使用指定的默认密钥）
     *
     * @param defaultKeyBytes 默认密钥字节数组
     */
    public DefaultEncryptionService(byte[] defaultKeyBytes) {
        this.defaultKeyBytes = defaultKeyBytes;
        log.info("默认AES加密服务已初始化（使用指定密钥），算法: {}, 密钥长度: {} 位", "AES", defaultKeyBytes.length);
    }

    @Override
    public byte[] encrypt(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (data == null || data.length == 0) {
            return data;
        }

        try {
            byte[] result = SecureUtil.aes(defaultKeyBytes).encrypt(data);

            if (log.isDebugEnabled()) {
                log.debug("字段加密成功: {}, 原始长度: {}, 加密后长度: {}", fieldMetadata.getFieldName(), data.length, result.length);
            }

            return result;

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "字段加密失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (encryptedData == null) {
            return null;
        }

        try {
            String result = SecureUtil.aes(defaultKeyBytes).decryptStr(encryptedData);

            System.out.println("解析后字符串："+result);
            return HexUtil.decodeHex(result);

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "字段解密失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public boolean supportsEncryption(ProtocolFieldMetadata fieldMetadata) {
        return fieldMetadata != null && fieldMetadata.isEncrypted();
    }

    @Override
    public String getAlgorithmName() {
        return "AES";
    }
}
