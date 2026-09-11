package com.lambda.cloud.crypto.key;

/**
 * 密钥解析扩展点
 * <p>
 * 按密钥标识解析 {@link KeyEntry}。默认实现为
 * {@link PropertiesKeyProvider}（从 {@code lambda.crypto.keys} 配置解析）；
 * 下游可注册自定义实现（如从 KMS / 配置中心加载）覆盖。
 *
 * @author Jin
 * @since 2026.1.1
 */
public interface KeyProvider {

    /**
     * 按密钥标识解析密钥
     *
     * @param keyId 密钥标识
     * @return 密钥条目，不存在时抛出 {@link com.lambda.cloud.crypto.exception.CryptoException}
     */
    KeyEntry get(String keyId);

    /**
     * 解析默认密钥（未指定 keyId 时使用）
     *
     * @return 密钥条目
     */
    KeyEntry get();
}
