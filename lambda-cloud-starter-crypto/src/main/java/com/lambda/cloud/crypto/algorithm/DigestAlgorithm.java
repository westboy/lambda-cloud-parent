package com.lambda.cloud.crypto.algorithm;

/**
 * 摘要算法类型
 *
 * @author Jin
 * @since 2026.1.1
 */
public enum DigestAlgorithm {

    /**
     * MD5（仅兼容存量数据，新场景禁用）
     */
    MD5,

    /**
     * SHA-1（仅兼容存量数据，新场景禁用）
     */
    SHA1,

    /**
     * SHA-256
     */
    SHA256,

    /**
     * SHA-512
     */
    SHA512,

    /**
     * SM3 国密摘要算法
     */
    SM3
}
