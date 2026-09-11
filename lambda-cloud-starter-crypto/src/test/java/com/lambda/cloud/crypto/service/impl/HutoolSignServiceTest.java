package com.lambda.cloud.crypto.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.hutool.crypto.KeyUtil;
import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.key.FixedKeyProvider;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.service.SignService;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

/**
 * {@link HutoolSignService} 加签/验签测试
 *
 * @author Jin
 * @since 2026.1.1
 */
class HutoolSignServiceTest {

    private static final byte[] DATA = "lambda-cloud-crypto-sign-test".getBytes(StandardCharsets.UTF_8);

    @Test
    void shouldSignAndVerifyWithRsa() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        SignService service = new HutoolSignService(new FixedKeyProvider(rsa("rsa", pair)));

        byte[] signature = service.sign(DATA, "rsa");

        assertThat(service.verify(DATA, signature, "rsa")).isTrue();
    }

    @Test
    void shouldSignAndVerifyWithSm2() {
        cn.hutool.crypto.asymmetric.SM2 sm2 = new cn.hutool.crypto.asymmetric.SM2();
        SignService service = new HutoolSignService(
                new FixedKeyProvider(KeyEntry.of("sm2", AlgorithmType.SM2, sm2.getPublicKey(), sm2.getPrivateKey())));

        byte[] signature = service.sign(DATA, "sm2");

        assertThat(service.verify(DATA, signature, "sm2")).isTrue();
    }

    @Test
    void shouldFailVerifyOnTamperedData() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        SignService service = new HutoolSignService(new FixedKeyProvider(rsa("rsa", pair)));

        byte[] signature = service.sign(DATA, "rsa");

        assertThat(service.verify("tampered".getBytes(StandardCharsets.UTF_8), signature, "rsa"))
                .isFalse();
    }

    @Test
    void shouldFailVerifyOnTamperedSignature() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        SignService service = new HutoolSignService(new FixedKeyProvider(rsa("rsa", pair)));

        byte[] signature = service.sign(DATA, "rsa");
        signature[0] ^= 0xFF;

        assertThat(service.verify(DATA, signature, "rsa")).isFalse();
    }

    @Test
    void shouldRejectSignWithoutPrivateKey() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        SignService service = new HutoolSignService(
                new FixedKeyProvider(KeyEntry.of("rsa-pub-only", AlgorithmType.RSA, pair.getPublic(), null)));

        assertThatThrownBy(() -> service.sign(DATA, "rsa-pub-only"))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("Private key required");
    }

    @Test
    void shouldRejectSignWithSymmetricKey() {
        SignService service = new HutoolSignService(
                new FixedKeyProvider(KeyEntry.of("aes", AlgorithmType.AES, new SecretKeySpec(new byte[32], "AES"))));

        assertThatThrownBy(() -> service.sign(DATA, "aes"))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("asymmetric");
    }

    @Test
    void shouldUseDefaultKey() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        SignService service = new HutoolSignService(new FixedKeyProvider(rsa("default", pair)));

        assertThat(service.verify(DATA, service.sign(DATA))).isTrue();
    }

    private KeyEntry rsa(String id, KeyPair pair) {
        return KeyEntry.of(id, AlgorithmType.RSA, pair.getPublic(), pair.getPrivate());
    }
}
