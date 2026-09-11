package com.lambda.cloud.crypto;

/**
 * 加密组件常量
 *
 * @author Jin
 * @since 2026.1.1
 */
public final class CryptoConstants {

    /**
     * GCM 模式 IV 长度（字节），NIST SP 800-38D 推荐 12 字节
     */
    public static final int GCM_IV_LENGTH = 12;

    /**
     * GCM 模式认证标签长度（位）
     */
    public static final int GCM_TAG_LENGTH_BITS = 128;

    /**
     * 混合加密随机数据密钥长度（字节，AES-256）
     */
    public static final int HYBRID_DATA_KEY_LENGTH = 32;

    private CryptoConstants() {}
}
