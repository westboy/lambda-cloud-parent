package com.lambda.cloud.crypto.service.impl;

import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.lambda.cloud.crypto.BcProvider;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.key.KeyProvider;
import com.lambda.cloud.crypto.service.SignService;
import java.security.Signature;
import java.security.SignatureException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 基于 hutool + BouncyCastle 的加签/验签默认实现
 * <p>
 * RSA 使用 SHA256withRSA（hutool），SM2 使用 SM3withSM2（BC provider，JCA）。
 * 注意：hutool 加密对象内部持有 {@link javax.crypto.Cipher}，非线程安全，故每次调用创建新实例。
 *
 * @author Jin
 * @since 2026.1.1
 */
public class HutoolSignService implements SignService {

    static {
        BcProvider.ensureRegistered();
    }

    private final KeyProvider keyProvider;

    public HutoolSignService(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public byte[] sign(byte[] data, String keyId) {
        KeyEntry entry = keyProvider.get(keyId);
        requireAsymmetric(entry);
        if (entry.getPrivateKey() == null) {
            throw new CryptoException("Private key required for signing, keyId: " + entry.getId());
        }
        try {
            return switch (entry.getType()) {
                case RSA ->
                    new Sign(SignAlgorithm.SHA256withRSA, entry.getPrivateKey(), entry.getPublicKey()).sign(data);
                case SM2 -> signSm2(data, entry);
                default -> throw new CryptoException("Unsupported sign algorithm: " + entry.getType());
            };
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Sign failed, keyId: " + entry.getId(), e);
        }
    }

    @Override
    public byte[] sign(byte[] data) {
        return sign(data, keyProvider.get().getId());
    }

    @Override
    public boolean verify(byte[] data, byte[] signature, String keyId) {
        KeyEntry entry = keyProvider.get(keyId);
        requireAsymmetric(entry);
        if (entry.getPublicKey() == null) {
            throw new CryptoException("Public key required for verifying, keyId: " + entry.getId());
        }
        try {
            return switch (entry.getType()) {
                case RSA ->
                    new Sign(SignAlgorithm.SHA256withRSA, entry.getPrivateKey(), entry.getPublicKey())
                            .verify(data, signature);
                case SM2 -> verifySm2(data, signature, entry);
                default -> throw new CryptoException("Unsupported sign algorithm: " + entry.getType());
            };
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Verify failed, keyId: " + entry.getId(), e);
        }
    }

    private void requireAsymmetric(KeyEntry entry) {
        if (!entry.getType().isAsymmetric()) {
            throw new CryptoException("Sign requires an asymmetric key (RSA/SM2), keyId: " + entry.getId());
        }
    }

    @Override
    public boolean verify(byte[] data, byte[] signature) {
        return verify(data, signature, keyProvider.get().getId());
    }

    private byte[] signSm2(byte[] data, KeyEntry entry) throws Exception {
        Signature signature = Signature.getInstance("SM3withSM2", BouncyCastleProvider.PROVIDER_NAME);
        signature.initSign(entry.getPrivateKey());
        signature.update(data);
        return signature.sign();
    }

    private boolean verifySm2(byte[] data, byte[] signatureBytes, KeyEntry entry) throws Exception {
        try {
            Signature signature = Signature.getInstance("SM3withSM2", BouncyCastleProvider.PROVIDER_NAME);
            signature.initVerify(entry.getPublicKey());
            signature.update(data);
            return signature.verify(signatureBytes);
        } catch (SignatureException e) {
            // 签名值格式非法视为验签失败
            return false;
        }
    }
}
