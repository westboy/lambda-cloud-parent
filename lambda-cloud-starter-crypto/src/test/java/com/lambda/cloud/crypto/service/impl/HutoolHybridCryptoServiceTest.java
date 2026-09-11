package com.lambda.cloud.crypto.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.hutool.crypto.KeyUtil;
import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.key.FixedKeyProvider;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.service.HybridCryptoService;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;

/**
 * {@link HutoolHybridCryptoService} 混合加解密测试
 *
 * @author Jin
 * @since 2026.1.1
 */
class HutoolHybridCryptoServiceTest {

    @Test
    void shouldEncryptAndDecryptLargePayloadWithRsa() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        HybridCryptoService service =
                hybridService(KeyEntry.of("rsa", AlgorithmType.RSA, pair.getPublic(), pair.getPrivate()));
        byte[] data = randomBytes(10 * 1024);

        byte[] ciphertext = service.encrypt(data, "rsa");

        assertThat(service.decrypt(ciphertext, "rsa")).isEqualTo(data);
    }

    @Test
    void shouldEncryptAndDecryptWithSm2() {
        cn.hutool.crypto.asymmetric.SM2 sm2 = new cn.hutool.crypto.asymmetric.SM2();
        HybridCryptoService service =
                hybridService(KeyEntry.of("sm2", AlgorithmType.SM2, sm2.getPublicKey(), sm2.getPrivateKey()));
        byte[] data = "hybrid-sm2-test".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = service.encrypt(data, "sm2");

        assertThat(service.decrypt(ciphertext, "sm2")).isEqualTo(data);
    }

    @Test
    void shouldFailDecryptOnTamperedCiphertext() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        HybridCryptoService service =
                hybridService(KeyEntry.of("rsa", AlgorithmType.RSA, pair.getPublic(), pair.getPrivate()));
        byte[] data = randomBytes(1024);

        byte[] ciphertext = service.encrypt(data, "rsa");
        ciphertext[ciphertext.length - 1] ^= 0xFF;

        assertThatThrownBy(() -> service.decrypt(ciphertext, "rsa")).isInstanceOf(CryptoException.class);
    }

    @Test
    void shouldFailDecryptOnTooShortCiphertext() {
        KeyPair pair = KeyUtil.generateKeyPair("RSA", 2048);
        HybridCryptoService service =
                hybridService(KeyEntry.of("rsa", AlgorithmType.RSA, pair.getPublic(), pair.getPrivate()));

        assertThatThrownBy(() -> service.decrypt(new byte[8], "rsa"))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("too short");
    }

    private HybridCryptoService hybridService(KeyEntry entry) {
        FixedKeyProvider keyProvider = new FixedKeyProvider(entry);
        return new HutoolHybridCryptoService(new HutoolAsymmetricCryptoService(keyProvider), keyProvider);
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }
}
