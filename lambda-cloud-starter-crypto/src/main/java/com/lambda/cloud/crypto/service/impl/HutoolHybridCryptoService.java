package com.lambda.cloud.crypto.service.impl;

import com.lambda.cloud.crypto.CryptoConstants;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.key.KeyProvider;
import com.lambda.cloud.crypto.service.AsymmetricCryptoService;
import com.lambda.cloud.crypto.service.HybridCryptoService;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 混合（信封）加解密默认实现
 * <p>
 * 加密：随机 AES-256 数据密钥 + AES-GCM 加密数据，数据密钥经
 * {@link AsymmetricCryptoService}（RSA-OAEP / SM2）封装。
 * <p>
 * 密文格式：{@code keyLen(2字节,大端) || encryptedKey || iv(12字节) || ciphertext}
 *
 * @author Jin
 * @since 2026.1.1
 */
public class HutoolHybridCryptoService implements HybridCryptoService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AsymmetricCryptoService asymmetricCryptoService;

    private final KeyProvider keyProvider;

    public HutoolHybridCryptoService(AsymmetricCryptoService asymmetricCryptoService, KeyProvider keyProvider) {
        this.asymmetricCryptoService = asymmetricCryptoService;
        this.keyProvider = keyProvider;
    }

    @Override
    public byte[] encrypt(byte[] data, String keyId) {
        byte[] dataKey = new byte[CryptoConstants.HYBRID_DATA_KEY_LENGTH];
        SECURE_RANDOM.nextBytes(dataKey);
        byte[] iv = new byte[CryptoConstants.GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        try {
            SecretKeySpec keySpec = new SecretKeySpec(dataKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(data);
            byte[] encryptedKey = asymmetricCryptoService.encrypt(dataKey, keyId);
            if (encryptedKey.length > 0xFFFF) {
                throw new CryptoException("Encrypted key too long: " + encryptedKey.length);
            }
            ByteBuffer buffer =
                    ByteBuffer.allocate(2 + encryptedKey.length + CryptoConstants.GCM_IV_LENGTH + ciphertext.length);
            buffer.putShort((short) encryptedKey.length);
            buffer.put(encryptedKey);
            buffer.put(iv);
            buffer.put(ciphertext);
            return buffer.array();
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Hybrid encrypt failed, keyId: " + keyId, e);
        }
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return encrypt(data, keyProvider.get().getId());
    }

    @Override
    public byte[] decrypt(byte[] data, String keyId) {
        if (data == null || data.length <= 2 + CryptoConstants.GCM_IV_LENGTH) {
            throw new CryptoException("Invalid hybrid ciphertext, too short");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int keyLen = buffer.getShort() & 0xFFFF;
            if (keyLen <= 0 || buffer.remaining() < keyLen + CryptoConstants.GCM_IV_LENGTH + 1) {
                throw new CryptoException("Invalid hybrid ciphertext, bad key length: " + keyLen);
            }
            byte[] encryptedKey = new byte[keyLen];
            buffer.get(encryptedKey);
            byte[] iv = new byte[CryptoConstants.GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            byte[] dataKey = asymmetricCryptoService.decrypt(encryptedKey, keyId);
            SecretKeySpec keySpec = new SecretKeySpec(dataKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Hybrid decrypt failed, keyId: " + keyId, e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return decrypt(data, keyProvider.get().getId());
    }
}
