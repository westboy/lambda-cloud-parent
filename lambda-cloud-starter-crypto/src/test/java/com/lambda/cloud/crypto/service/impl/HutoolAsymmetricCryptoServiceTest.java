package com.lambda.cloud.crypto.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.hutool.crypto.KeyUtil;
import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.key.FixedKeyProvider;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.service.AsymmetricCryptoService;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import org.junit.jupiter.api.Test;

/**
 * {@link HutoolAsymmetricCryptoService} 非对称加解密测试
 *
 * @author Jin
 * @since 2026.1.1
 */
class HutoolAsymmetricCryptoServiceTest {

    private static final byte[] DATA = "lambda-cloud-crypto-asymmetric-test".getBytes(StandardCharsets.UTF_8);

    @Test
    void shouldEncryptAndDecryptWithRsaOaep() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        AsymmetricCryptoService service = new HutoolAsymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("rsa", AlgorithmType.RSA, pair.getPublic(), pair.getPrivate())));

        byte[] ciphertext = service.encrypt(DATA, "rsa");

        assertThat(service.decrypt(ciphertext, "rsa")).isEqualTo(DATA);
    }

    @Test
    void shouldEncryptAndDecryptWithSm2() {
        cn.hutool.crypto.asymmetric.SM2 sm2 = new cn.hutool.crypto.asymmetric.SM2();
        AsymmetricCryptoService service = new HutoolAsymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("sm2", AlgorithmType.SM2, sm2.getPublicKey(), sm2.getPrivateKey())));

        byte[] ciphertext = service.encrypt(DATA, "sm2");

        assertThat(service.decrypt(ciphertext, "sm2")).isEqualTo(DATA);
    }

    @Test
    void shouldFailDecryptOnTamperedCiphertext() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        AsymmetricCryptoService service = new HutoolAsymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("rsa", AlgorithmType.RSA, pair.getPublic(), pair.getPrivate())));

        byte[] ciphertext = service.encrypt(DATA, "rsa");
        ciphertext[ciphertext.length - 1] ^= 0xFF;

        assertThatThrownBy(() -> service.decrypt(ciphertext, "rsa")).isInstanceOf(CryptoException.class);
    }

    @Test
    void shouldRejectEncryptWithoutPublicKey() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        AsymmetricCryptoService service = new HutoolAsymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("rsa", AlgorithmType.RSA, null, pair.getPrivate())));

        assertThatThrownBy(() -> service.encrypt(DATA, "rsa"))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("Public key required");
    }
}
