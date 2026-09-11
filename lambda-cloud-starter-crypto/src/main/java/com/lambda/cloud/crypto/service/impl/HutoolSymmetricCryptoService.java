package com.lambda.cloud.crypto.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.lambda.cloud.crypto.BcProvider;
import com.lambda.cloud.crypto.CryptoConstants;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.key.KeyProvider;
import com.lambda.cloud.crypto.service.SymmetricCryptoService;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

/**
 * 基于 JCE + BouncyCastle 的对称加解密默认实现
 * <p>
 * AES 密钥使用 AES-GCM，SM4 密钥使用 SM4-GCM；
 * 每次加密生成随机 IV 并前置到密文（{@code iv || ciphertext}）。
 *
 * @author Jin
 * @since 2026.1.1
 */
public class HutoolSymmetricCryptoService implements SymmetricCryptoService {

    static {
        BcProvider.ensureRegistered();
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final KeyProvider keyProvider;

    public HutoolSymmetricCryptoService(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public byte[] encrypt(byte[] data, String keyId) {
        KeyEntry entry = keyProvider.get(keyId);
        if (entry.getSecretKey() == null) {
            throw new CryptoException("Secret key required, keyId: " + entry.getId());
        }
        byte[] iv = new byte[CryptoConstants.GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        try {
            Cipher cipher = createCipher(entry);
            cipher.init(Cipher.ENCRYPT_MODE, entry.getSecretKey(), paramsSpec(iv));
            return ArrayUtil.addAll(iv, cipher.doFinal(data));
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Symmetric encrypt failed, keyId: " + entry.getId(), e);
        }
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return encrypt(data, keyProvider.get().getId());
    }

    @Override
    public byte[] decrypt(byte[] data, String keyId) {
        KeyEntry entry = keyProvider.get(keyId);
        if (entry.getSecretKey() == null) {
            throw new CryptoException("Secret key required, keyId: " + entry.getId());
        }
        if (data == null || data.length <= CryptoConstants.GCM_IV_LENGTH) {
            throw new CryptoException("Invalid ciphertext, too short, keyId: " + entry.getId());
        }
        byte[] iv = ArrayUtil.sub(data, 0, CryptoConstants.GCM_IV_LENGTH);
        byte[] ciphertext = ArrayUtil.sub(data, CryptoConstants.GCM_IV_LENGTH, data.length);
        try {
            Cipher cipher = createCipher(entry);
            cipher.init(Cipher.DECRYPT_MODE, entry.getSecretKey(), paramsSpec(iv));
            return cipher.doFinal(ciphertext);
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Symmetric decrypt failed, keyId: " + entry.getId(), e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return decrypt(data, keyProvider.get().getId());
    }

    /**
     * AES 走默认 JCE provider，SM4 需要 BouncyCastle provider
     */
    private Cipher createCipher(KeyEntry entry) throws Exception {
        return switch (entry.getType()) {
            case AES -> Cipher.getInstance("AES/GCM/NoPadding");
            case SM4 ->
                Cipher.getInstance(
                        "SM4/GCM/NoPadding", org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME);
            default ->
                throw new CryptoException(
                        "Symmetric crypto requires a symmetric key (AES/SM4), got: " + entry.getType());
        };
    }

    private GCMParameterSpec paramsSpec(byte[] iv) {
        return new GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH_BITS, iv);
    }
}
