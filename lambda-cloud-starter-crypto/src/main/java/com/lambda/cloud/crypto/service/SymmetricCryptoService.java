package com.lambda.cloud.crypto.service;

import com.lambda.cloud.crypto.algorithm.AlgorithmType;

/**
 * 对称加解密服务扩展点
 * <p>
 * 算法由密钥类型决定：AES 密钥使用 AES-GCM，SM4 密钥使用 SM4-GCM。
 * 每次加密生成随机 IV 并前置到密文（{@code iv || ciphertext}）。
 *
 * @author Jin
 * @since 2026.1.1
 */
public interface SymmetricCryptoService {

    /**
     * 使用指定密钥加密（需要对称密钥）
     *
     * @param data 原始数据
     * @param keyId 密钥标识，需为 {@link AlgorithmType#AES} 或 {@link AlgorithmType#SM4} 类型
     * @return 密文（IV 前置）
     */
    byte[] encrypt(byte[] data, String keyId);

    /**
     * 使用默认密钥加密
     *
     * @param data 原始数据
     * @return 密文（IV 前置）
     */
    byte[] encrypt(byte[] data);

    /**
     * 使用指定密钥解密
     *
     * @param data 密文（IV 前置）
     * @param keyId 密钥标识
     * @return 原始数据
     */
    byte[] decrypt(byte[] data, String keyId);

    /**
     * 使用默认密钥解密
     *
     * @param data 密文（IV 前置）
     * @return 原始数据
     */
    byte[] decrypt(byte[] data);
}
