package com.lambda.cloud.netty.protocol.encryption;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;

/**
 * 加密服务接口
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
     * 获取加密算法名称
     *
     * @return 加密算法名称
     */
    String getAlgorithmName();
}
