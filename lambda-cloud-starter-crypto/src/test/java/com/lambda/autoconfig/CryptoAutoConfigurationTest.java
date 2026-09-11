package com.lambda.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import cn.hutool.crypto.KeyUtil;
import com.lambda.cloud.crypto.key.KeyProvider;
import com.lambda.cloud.crypto.service.AsymmetricCryptoService;
import com.lambda.cloud.crypto.service.DigestService;
import com.lambda.cloud.crypto.service.HybridCryptoService;
import com.lambda.cloud.crypto.service.SignService;
import com.lambda.cloud.crypto.service.SymmetricCryptoService;
import java.security.KeyPair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link CryptoAutoConfiguration} 装配测试
 *
 * @author Jin
 * @since 2026.1.1
 */
class CryptoAutoConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(CryptoAutoConfiguration.class));

    @Test
    void shouldRegisterAllServicesByDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(KeyProvider.class);
            assertThat(context).hasSingleBean(DigestService.class);
            assertThat(context).hasSingleBean(SignService.class);
            assertThat(context).hasSingleBean(AsymmetricCryptoService.class);
            assertThat(context).hasSingleBean(SymmetricCryptoService.class);
            assertThat(context).hasSingleBean(HybridCryptoService.class);
        });
    }

    @Test
    void shouldNotRegisterWhenDisabled() {
        runner.withPropertyValues("lambda.crypto.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(KeyProvider.class);
            assertThat(context).doesNotHaveBean(DigestService.class);
            assertThat(context).doesNotHaveBean(SignService.class);
        });
    }

    @Test
    void shouldFailFastOnInvalidKeyConfig() {
        runner.withPropertyValues(
                        "lambda.crypto.keys[0].id=broken",
                        "lambda.crypto.keys[0].type=RSA",
                        "lambda.crypto.keys[0].public-key=not-a-valid-key")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void shouldAllowCustomKeyProviderOverride() {
        runner.withUserConfiguration(CustomKeyProviderConfiguration.class).run(context -> {
            assertThat(context).getBean(KeyProvider.class).isInstanceOf(CustomKeyProvider.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomKeyProviderConfiguration {

        @Bean
        public KeyProvider keyProvider() {
            KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
            return new CustomKeyProvider(pair);
        }
    }

    static class CustomKeyProvider implements KeyProvider {

        private final com.lambda.cloud.crypto.key.KeyEntry entry;

        CustomKeyProvider(KeyPair pair) {
            this.entry = com.lambda.cloud.crypto.key.KeyEntry.of(
                    "default",
                    com.lambda.cloud.crypto.algorithm.AlgorithmType.RSA,
                    pair.getPublic(),
                    pair.getPrivate());
        }

        @Override
        public com.lambda.cloud.crypto.key.KeyEntry get(String keyId) {
            return entry;
        }

        @Override
        public com.lambda.cloud.crypto.key.KeyEntry get() {
            return entry;
        }
    }
}
