package com.lambda.cloud.crypto.service;

import com.lambda.cloud.crypto.algorithm.AlgorithmType;

/**
 * 混合（信封）加解密服务扩展点
 * <p>
 * 解决非对称算法明文长度受限的问题：每次加密生成随机 AES-256 数据密钥，
 * 数据用 AES-GCM 加密，数据密钥用指定非对称密钥封装。
 * <p>
 * 密文格式：{@code keyLen(2字节,大端) || encryptedKey || iv(12字节) || ciphertext}
 *
 * @author Jin
 * @since 2026.1.1
 */
public interface HybridCryptoService {

    /**
     * 混合加密（需要指定密钥的公钥）
     *
     * @param data 原始数据，长度不限
     * @param keyId 非对称密钥标识，需为 {@link AlgorithmType#RSA} 或 {@link AlgorithmType#SM2} 类型
     * @return 信封密文
     */
    byte[] encrypt(byte[] data, String keyId);

    /**
     * 使用默认密钥混合加密
     *
     * @param data 原始数据
     * @return 信封密文
     */
    byte[] encrypt(byte[] data);

    /**
     * 混合解密（需要指定密钥的私钥）
     *
     * @param data 信封密文
     * @param keyId 非对称密钥标识
     * @return 原始数据
     */
    byte[] decrypt(byte[] data, String keyId);

    /**
     * 使用默认密钥混合解密
     *
     * @param data 信封密文
     * @return 原始数据
     */
    byte[] decrypt(byte[] data);
}
