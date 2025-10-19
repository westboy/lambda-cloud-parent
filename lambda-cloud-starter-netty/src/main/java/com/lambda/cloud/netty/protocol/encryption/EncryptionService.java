package com.lambda.cloud.netty.protocol.encryption;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;

/**
 * 加密服务接口
 * <p>
 * 定义协议字段加密解密的统一标准，支持多种加密算法和密钥管理策略
 * </p>
 *
 * @author Jin
 */
public interface EncryptionService {

    /**
     * 加密数据
     *
     * @param data          原始数据
     * @param fieldMetadata 字段元数据（包含加密配置信息）
     * @return 加密后的数据
     * @throws ProtocolException 加密异常
     */
    byte[] encrypt(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException;

    /**
     * 解密数据
     *
     * @param encryptedData 加密数据
     * @param fieldMetadata 字段元数据（包含解密配置信息）
     * @return 解密后的原始数据
     * @throws ProtocolException 解密异常
     */
    byte[] decrypt(byte[] encryptedData, ProtocolFieldMetadata fieldMetadata) throws ProtocolException;

    /**
     * 检查是否支持指定字段的加密
     *
     * @param fieldMetadata 字段元数据
     * @return true表示支持，false表示不支持
     */
    boolean supportsEncryption(ProtocolFieldMetadata fieldMetadata);

    /**
     * 获取加密算法名称
     *
     * @return 加密算法名称
     */
    String getAlgorithmName();

    /**
     * 获取密钥长度（位）
     *
     * @return 密钥长度
     */
    int getKeyLength();

    /**
     * 验证加密配置
     *
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 配置验证异常
     */
    default void validateConfiguration(ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (!supportsEncryption(fieldMetadata)) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.VALIDATION_ERROR,
                    "不支持的加密配置: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName());
        }
    }
}
