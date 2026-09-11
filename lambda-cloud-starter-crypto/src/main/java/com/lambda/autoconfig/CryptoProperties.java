package com.lambda.autoconfig;

import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加密组件配置属性
 * <p>
 * 密钥值支持环境变量注入（如 {@code ${CRYPTO_RSA_PRIVATE_KEY}}），禁止提交真实密钥。
 *
 * <pre>{@code
 * lambda:
 *   crypto:
 *     enabled: true
 *     default-key-id: default
 *     keys:
 *       - id: default
 *         type: RSA
 *         public-key: ${CRYPTO_RSA_PUBLIC_KEY}      # PEM 或 Base64
 *         private-key: ${CRYPTO_RSA_PRIVATE_KEY}
 *       - id: sm2-cert
 *         type: SM2
 *         key-store:
 *           type: PKCS12
 *           location: ${CRYPTO_JKS_PATH}
 *           password: ${CRYPTO_JKS_PASSWORD}
 *           alias: sign-key
 * }</pre>
 *
 * @author Jin
 * @since 2026.1.1
 */
@Data
@ConfigurationProperties(prefix = "lambda.crypto")
public class CryptoProperties {

    /**
     * 是否启用加密组件
     */
    private Boolean enabled;

    /**
     * 默认密钥标识（调用服务未指定 keyId 时使用）
     */
    private String defaultKeyId = "default";

    /**
     * 密钥列表
     */
    private List<KeyProperties> keys = new ArrayList<>();

    /**
     * 密钥配置
     * <p>
     * 非对称密钥（RSA/SM2）二选一或同时配置 {@code publicKey}/{@code privateKey}：
     * 只配公钥可用于验签/加密，只配私钥可用于签名/解密。
     */
    @Data
    public static class KeyProperties {

        /**
         * 密钥标识（唯一）
         */
        private String id;

        /**
         * 算法类型：RSA / SM2 / AES / SM4
         */
        private AlgorithmType type;

        /**
         * 公钥（RSA: PEM 或 Base64；SM2: hex）
         */
        private String publicKey;

        /**
         * 私钥（RSA: PEM 或 Base64；SM2: hex）
         */
        private String privateKey;

        /**
         * 对称密钥（AES/SM4: Base64 或 hex）
         */
        private String secretKey;

        /**
         * 密钥库加载（与内联公私钥配置互斥，优先生效）
         */
        private KeyStoreProperties keyStore;
    }

    /**
     * 密钥库配置（JKS / PKCS12）
     */
    @Data
    public static class KeyStoreProperties {

        /**
         * 密钥库类型：JKS / PKCS12
         */
        private String type = "JKS";

        /**
         * 密钥库位置（文件路径，找不到文件时回退 classpath 资源）
         */
        private String location;

        /**
         * 密钥库密码（经环境变量注入）
         */
        private String password;

        /**
         * 密钥别名
         */
        private String alias;
    }
}
