package com.lambda.autoconfig;

import com.lambda.cloud.crypto.key.KeyProvider;
import com.lambda.cloud.crypto.key.PropertiesKeyProvider;
import com.lambda.cloud.crypto.service.AsymmetricCryptoService;
import com.lambda.cloud.crypto.service.DigestService;
import com.lambda.cloud.crypto.service.HybridCryptoService;
import com.lambda.cloud.crypto.service.SignService;
import com.lambda.cloud.crypto.service.SymmetricCryptoService;
import com.lambda.cloud.crypto.service.impl.HutoolAsymmetricCryptoService;
import com.lambda.cloud.crypto.service.impl.HutoolDigestService;
import com.lambda.cloud.crypto.service.impl.HutoolHybridCryptoService;
import com.lambda.cloud.crypto.service.impl.HutoolSignService;
import com.lambda.cloud.crypto.service.impl.HutoolSymmetricCryptoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 加密组件自动配置
 * <p>
 * 提供加签/验签、非对称/对称/混合加解密与摘要能力的统一装配。
 * 所有服务均为可覆盖扩展点（{@code @ConditionalOnMissingBean}），
 * 下游可注册自定义实现替换默认的 hutool 实现。
 *
 * @author Jin
 * @since 2026.1.1
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CryptoProperties.class)
@ConditionalOnClass(cn.hutool.crypto.SecureUtil.class)
@ConditionalOnProperty(prefix = "lambda.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CryptoAutoConfiguration {

    /**
     * 密钥解析器，从 {@code lambda.crypto.keys} 配置解析密钥。
     * <p>
     * 解析失败在启动期抛出 {@link com.lambda.cloud.crypto.exception.CryptoException}（fail-fast）。
     */
    @Bean
    @ConditionalOnMissingBean(KeyProvider.class)
    public KeyProvider keyProvider(CryptoProperties properties) {
        log.info(
                "Initializing crypto key provider, key count: {}",
                properties.getKeys().size());
        return new PropertiesKeyProvider(properties);
    }

    /**
     * 摘要服务
     */
    @Bean
    @ConditionalOnMissingBean(DigestService.class)
    public DigestService digestService() {
        return new HutoolDigestService();
    }

    /**
     * 加签/验签服务（RSA: SHA256withRSA，SM2: SM3withSM2）
     */
    @Bean
    @ConditionalOnMissingBean(SignService.class)
    public SignService signService(KeyProvider keyProvider) {
        return new HutoolSignService(keyProvider);
    }

    /**
     * 非对称加解密服务（RSA-OAEP / SM2）
     */
    @Bean
    @ConditionalOnMissingBean(AsymmetricCryptoService.class)
    public AsymmetricCryptoService asymmetricCryptoService(KeyProvider keyProvider) {
        return new HutoolAsymmetricCryptoService(keyProvider);
    }

    /**
     * 对称加解密服务（AES-GCM / SM4-GCM，随机 IV 前置）
     */
    @Bean
    @ConditionalOnMissingBean(SymmetricCryptoService.class)
    public SymmetricCryptoService symmetricCryptoService(KeyProvider keyProvider) {
        return new HutoolSymmetricCryptoService(keyProvider);
    }

    /**
     * 混合（信封）加解密服务：随机 AES 密钥 + 非对称封装
     */
    @Bean
    @ConditionalOnMissingBean(HybridCryptoService.class)
    public HybridCryptoService hybridCryptoService(
            AsymmetricCryptoService asymmetricCryptoService, KeyProvider keyProvider) {
        return new HutoolHybridCryptoService(asymmetricCryptoService, keyProvider);
    }
}
