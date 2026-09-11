package com.lambda.security.web.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.KeyUtil;
import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.key.KeyProvider;
import com.lambda.cloud.crypto.service.AsymmetricCryptoService;
import com.lambda.cloud.crypto.service.impl.HutoolAsymmetricCryptoService;
import com.lambda.security.exception.AuthenticationException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import org.junit.jupiter.api.Test;

/**
 * {@link AsymmetricCredentialsDecryptor} 登录凭据解密测试
 *
 * @author Jin
 * @since 2026.1.1
 */
class AsymmetricCredentialsDecryptorTest {

    private static final KeyPair RSA_PAIR = KeyUtil.generateKeyPair("RSA", 2048);

    private final AsymmetricCredentialsDecryptor decryptor =
            new AsymmetricCredentialsDecryptor(asymmetricCryptoService(), "login");

    @Test
    void shouldDecryptEncryptedCredentials() {
        String plaintext = "admin-密码-123";
        String encrypted =
                Base64.encode(asymmetricCryptoService().encrypt(plaintext.getBytes(StandardCharsets.UTF_8), "login"));

        assertThat(decryptor.decrypt(encrypted)).isEqualTo(plaintext);
    }

    @Test
    void shouldPassThroughBlankValue() {
        assertThat(decryptor.decrypt(null)).isNull();
        assertThat(decryptor.decrypt("")).isEmpty();
        assertThat(decryptor.decrypt("  ")).isEqualTo("  ");
    }

    @Test
    void shouldThrowGenericAuthenticationExceptionOnInvalidCiphertext() {
        assertThatThrownBy(() -> decryptor.decrypt(Base64.encode("not-a-valid-ciphertext")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("登录凭据解密失败");
    }

    private AsymmetricCryptoService asymmetricCryptoService() {
        KeyEntry entry = KeyEntry.of("login", AlgorithmType.RSA, RSA_PAIR.getPublic(), RSA_PAIR.getPrivate());
        return new HutoolAsymmetricCryptoService(new KeyProvider() {
            @Override
            public KeyEntry get(String keyId) {
                return entry;
            }

            @Override
            public KeyEntry get() {
                return entry;
            }
        });
    }
}
