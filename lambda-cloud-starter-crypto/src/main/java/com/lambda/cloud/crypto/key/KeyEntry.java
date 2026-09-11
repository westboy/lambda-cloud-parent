package com.lambda.cloud.crypto.key;

import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import lombok.Builder;
import lombok.Getter;

/**
 * 密钥条目
 * <p>
 * 一次解析后的密钥材料。非对称密钥可只含公钥（验签/加密）或只含私钥（签名/解密）；
 * 对称密钥必须含 {@code secretKey}。
 *
 * @author Jin
 * @since 2026.1.1
 */
@Getter
@Builder
public class KeyEntry {

    /**
     * 密钥标识
     */
    private final String id;

    /**
     * 算法类型
     */
    private final AlgorithmType type;

    /**
     * 公钥（非对称密钥，可为 null）
     */
    private final PublicKey publicKey;

    /**
     * 私钥（非对称密钥，可为 null）
     */
    private final PrivateKey privateKey;

    /**
     * 对称密钥（AES/SM4，可为 null）
     */
    private final SecretKey secretKey;

    /**
     * 非对称密钥条目
     */
    public static KeyEntry of(String id, AlgorithmType type, PublicKey publicKey, PrivateKey privateKey) {
        return KeyEntry.builder()
                .id(id)
                .type(type)
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
    }

    /**
     * 对称密钥条目
     */
    public static KeyEntry of(String id, AlgorithmType type, SecretKey secretKey) {
        return KeyEntry.builder().id(id).type(type).secretKey(secretKey).build();
    }
}
