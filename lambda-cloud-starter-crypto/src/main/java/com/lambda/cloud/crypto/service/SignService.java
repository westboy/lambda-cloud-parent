package com.lambda.cloud.crypto.service;

import com.lambda.cloud.crypto.algorithm.AlgorithmType;

/**
 * 加签/验签服务扩展点
 * <p>
 * 签名算法由密钥类型决定：RSA 密钥使用 SHA256withRSA，SM2 密钥使用 SM3withSM2。
 *
 * @author Jin
 * @since 2026.1.1
 */
public interface SignService {

    /**
     * 使用指定密钥签名（需要私钥）
     *
     * @param data 原始数据
     * @param keyId 密钥标识，需为 {@link AlgorithmType#RSA} 或 {@link AlgorithmType#SM2} 类型
     * @return 签名值
     */
    byte[] sign(byte[] data, String keyId);

    /**
     * 使用默认密钥签名
     *
     * @param data 原始数据
     * @return 签名值
     */
    byte[] sign(byte[] data);

    /**
     * 使用指定密钥验签（需要公钥）
     *
     * @param data 原始数据
     * @param signature 签名值
     * @param keyId 密钥标识
     * @return 验签是否通过
     */
    boolean verify(byte[] data, byte[] signature, String keyId);

    /**
     * 使用默认密钥验签
     *
     * @param data 原始数据
     * @param signature 签名值
     * @return 验签是否通过
     */
    boolean verify(byte[] data, byte[] signature);
}
