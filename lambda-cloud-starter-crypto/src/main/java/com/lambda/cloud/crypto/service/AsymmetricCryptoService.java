package com.lambda.cloud.crypto.service;

import com.lambda.cloud.crypto.algorithm.AlgorithmType;

/**
 * 非对称加解密服务扩展点
 * <p>
 * 算法由密钥类型决定：RSA 密钥使用 RSA-OAEP（SHA-256），SM2 密钥使用 SM2（C1C3C2）。
 *
 * @author Jin
 * @since 2026.1.1
 */
public interface AsymmetricCryptoService {

    /**
     * 使用指定密钥的公钥加密（需要公钥）
     *
     * @param data 原始数据
     * @param keyId 密钥标识，需为 {@link AlgorithmType#RSA} 或 {@link AlgorithmType#SM2} 类型
     * @return 密文
     */
    byte[] encrypt(byte[] data, String keyId);

    /**
     * 使用默认密钥的公钥加密
     *
     * @param data 原始数据
     * @return 密文
     */
    byte[] encrypt(byte[] data);

    /**
     * 使用指定密钥的私钥解密（需要私钥）
     *
     * @param data 密文
     * @param keyId 密钥标识
     * @return 原始数据
     */
    byte[] decrypt(byte[] data, String keyId);

    /**
     * 使用默认密钥的私钥解密
     *
     * @param data 密文
     * @return 原始数据
     */
    byte[] decrypt(byte[] data);
}
