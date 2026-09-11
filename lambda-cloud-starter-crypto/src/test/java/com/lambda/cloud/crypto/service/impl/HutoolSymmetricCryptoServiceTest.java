package com.lambda.cloud.crypto.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.key.FixedKeyProvider;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.service.SymmetricCryptoService;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

/**
 * {@link HutoolSymmetricCryptoService} 对称加解密测试
 *
 * @author Jin
 * @since 2026.1.1
 */
class HutoolSymmetricCryptoServiceTest {

    private static final byte[] DATA = "lambda-cloud-crypto-symmetric-test".getBytes(StandardCharsets.UTF_8);

    @Test
    void shouldEncryptAndDecryptWithAesGcm() {
        SymmetricCryptoService service = new HutoolSymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("aes", AlgorithmType.AES, secret(32, "AES"))));

        byte[] ciphertext = service.encrypt(DATA, "aes");

        assertThat(service.decrypt(ciphertext, "aes")).isEqualTo(DATA);
    }

    @Test
    void shouldEncryptAndDecryptWithSm4Gcm() {
        SymmetricCryptoService service = new HutoolSymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("sm4", AlgorithmType.SM4, secret(16, "SM4"))));

        byte[] ciphertext = service.encrypt(DATA, "sm4");

        assertThat(service.decrypt(ciphertext, "sm4")).isEqualTo(DATA);
    }

    @Test
    void shouldProduceDifferentCiphertextEachTime() {
        SymmetricCryptoService service = new HutoolSymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("aes", AlgorithmType.AES, secret(32, "AES"))));

        byte[] first = service.encrypt(DATA, "aes");
        byte[] second = service.encrypt(DATA, "aes");

        assertThat(first).isNotEqualTo(second);
        assertThat(service.decrypt(first, "aes")).isEqualTo(DATA);
        assertThat(service.decrypt(second, "aes")).isEqualTo(DATA);
    }

    @Test
    void shouldFailDecryptOnTamperedCiphertext() {
        SymmetricCryptoService service = new HutoolSymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("aes", AlgorithmType.AES, secret(32, "AES"))));

        byte[] ciphertext = service.encrypt(DATA, "aes");
        ciphertext[ciphertext.length - 1] ^= 0xFF;

        assertThatThrownBy(() -> service.decrypt(ciphertext, "aes")).isInstanceOf(CryptoException.class);
    }

    @Test
    void shouldFailDecryptOnTooShortCiphertext() {
        SymmetricCryptoService service = new HutoolSymmetricCryptoService(
                new FixedKeyProvider(KeyEntry.of("aes", AlgorithmType.AES, secret(32, "AES"))));

        assertThatThrownBy(() -> service.decrypt(new byte[10], "aes"))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("too short");
    }

    private SecretKeySpec secret(int length, String algorithm) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return new SecretKeySpec(bytes, algorithm);
    }
}
