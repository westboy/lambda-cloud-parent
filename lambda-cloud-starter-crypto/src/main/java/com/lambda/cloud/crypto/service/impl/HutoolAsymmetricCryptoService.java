package com.lambda.cloud.crypto.service.impl;

import cn.hutool.crypto.asymmetric.AsymmetricCrypto;
import cn.hutool.crypto.asymmetric.KeyType;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.key.KeyProvider;
import com.lambda.cloud.crypto.service.AsymmetricCryptoService;

/**
 * 基于 hutool 的非对称加解密默认实现
 * <p>
 * RSA 使用 RSA-OAEP（SHA-256），SM2 使用 C1C3C2 模式；
 * 长明文场景请使用 {@link HutoolHybridCryptoService}。
 *
 * @author Jin
 * @since 2026.1.1
 */
public class HutoolAsymmetricCryptoService implements AsymmetricCryptoService {

    private final KeyProvider keyProvider;

    public HutoolAsymmetricCryptoService(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public byte[] encrypt(byte[] data, String keyId) {
        KeyEntry entry = keyProvider.get(keyId);
        if (entry.getPublicKey() == null) {
            throw new CryptoException("Public key required for encryption, keyId: " + entry.getId());
        }
        try {
            return createCrypto(entry).encrypt(data, KeyType.PublicKey);
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Asymmetric encrypt failed, keyId: " + entry.getId(), e);
        }
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return encrypt(data, keyProvider.get().getId());
    }

    @Override
    public byte[] decrypt(byte[] data, String keyId) {
        KeyEntry entry = keyProvider.get(keyId);
        if (entry.getPrivateKey() == null) {
            throw new CryptoException("Private key required for decryption, keyId: " + entry.getId());
        }
        try {
            return createCrypto(entry).decrypt(data, KeyType.PrivateKey);
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Asymmetric decrypt failed, keyId: " + entry.getId(), e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return decrypt(data, keyProvider.get().getId());
    }

    private AsymmetricCrypto createCrypto(KeyEntry entry) {
        return switch (entry.getType()) {
            case RSA ->
                new AsymmetricCrypto(
                        "RSA/ECB/OAEPWithSHA-256AndMGF1Padding", entry.getPrivateKey(), entry.getPublicKey());
            case SM2 -> new AsymmetricCrypto("SM2", entry.getPrivateKey(), entry.getPublicKey());
            default ->
                throw new CryptoException(
                        "Asymmetric crypto requires an asymmetric key (RSA/SM2), got: " + entry.getType());
        };
    }
}
