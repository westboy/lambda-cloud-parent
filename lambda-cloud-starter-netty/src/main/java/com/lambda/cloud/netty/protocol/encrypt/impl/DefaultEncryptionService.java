package com.lambda.cloud.netty.protocol.encrypt.impl;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.encrypt.EncryptionService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认AES加密服务实现
 * <p>
 * 使用AES-256-GCM算法提供高安全性的加密解密服务，支持字段级别的密钥管理
 * </p>
 *
 * @author Jin
 */
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP")
public class DefaultEncryptionService implements EncryptionService {

    private final byte[] defaultKeyBytes;
    private final ThreadLocal<AES> aesThreadLocal;

    /**
     * 构造函数（使用指定的默认密钥）
     *
     * @param defaultKeyBytes 默认密钥字节数组
     */
    public DefaultEncryptionService(byte[] defaultKeyBytes) {
        this.defaultKeyBytes = defaultKeyBytes;
        // 使用ThreadLocal缓存AES实例，避免频繁创建Cipher带来的性能开销
        // Cipher非线程安全，因此每个线程维护一个独立的AES实例
        this.aesThreadLocal = ThreadLocal.withInitial(() -> SecureUtil.aes(defaultKeyBytes));
        log.info("默认AES加密服务已初始化（使用指定密钥），算法: {}, 密钥长度: {} 位", "AES", defaultKeyBytes.length);
    }

    public byte[] defaultKeyBytes() {
        return defaultKeyBytes;
    }

    @Override
    public byte[] encrypt(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (data == null || data.length == 0) {
            return data;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "字段 {} 加密前数据(Hex): {}",
                        fieldMetadata.getFieldName(),
                        HexUtil.encodeHexStr(data).toUpperCase());
            }

            // 使用ThreadLocal获取AES实例进行加密
            byte[] result = aesThreadLocal.get().encrypt(data);

            if (log.isDebugEnabled()) {
                log.debug(
                        "字段 {} 加密后数据(Hex): {}",
                        fieldMetadata.getFieldName(),
                        HexUtil.encodeHexStr(result).toUpperCase());
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
            // 使用ThreadLocal获取AES实例进行解密
            byte[] result = aesThreadLocal.get().decrypt(encryptedData);

            if (log.isDebugEnabled()) {
                log.debug(
                        "字段 {} 解密后数据(Hex): {}",
                        fieldMetadata.getFieldName(),
                        HexUtil.encodeHexStr(result).toUpperCase());
            }
            return result;

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "字段解密失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "AES";
    }
}
