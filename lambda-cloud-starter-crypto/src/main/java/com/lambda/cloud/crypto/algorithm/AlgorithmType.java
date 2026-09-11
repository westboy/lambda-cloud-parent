package com.lambda.cloud.crypto.algorithm;

import lombok.Getter;

/**
 * 密钥算法类型
 *
 * @author Jin
 * @since 2026.1.1
 */
@Getter
public enum AlgorithmType {

    /**
     * RSA 非对称算法（签名 SHA256withRSA，加密 RSA-OAEP）
     */
    RSA(true),

    /**
     * SM2 国密非对称算法（签名 SM3withSM2，加密 C1C3C2）
     */
    SM2(true),

    /**
     * AES 对称算法（AES-GCM）
     */
    AES(false),

    /**
     * SM4 国密对称算法（SM4-GCM）
     */
    SM4(false);

    /**
     * -- GETTER --
     *  是否为非对称算法
     */
    private final boolean asymmetric;

    AlgorithmType(boolean asymmetric) {
        this.asymmetric = asymmetric;
    }
}
