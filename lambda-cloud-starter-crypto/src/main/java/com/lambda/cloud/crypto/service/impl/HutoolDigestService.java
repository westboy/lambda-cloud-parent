package com.lambda.cloud.crypto.service.impl;

import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.digest.SM3;
import com.lambda.cloud.crypto.BcProvider;
import com.lambda.cloud.crypto.algorithm.DigestAlgorithm;
import com.lambda.cloud.crypto.exception.CryptoException;
import com.lambda.cloud.crypto.service.DigestService;

/**
 * 基于 hutool 的摘要默认实现（SM3 经 BouncyCastle provider）
 *
 * @author Jin
 * @since 2026.1.1
 */
public class HutoolDigestService implements DigestService {

    static {
        BcProvider.ensureRegistered();
    }

    @Override
    public byte[] digest(byte[] data, DigestAlgorithm algorithm) {
        if (algorithm == null) {
            throw new CryptoException("Digest algorithm must not be null");
        }
        try {
            if (algorithm == DigestAlgorithm.SM3) {
                return new SM3().digest(data);
            }
            Digester digester = new Digester(cn.hutool.crypto.digest.DigestAlgorithm.valueOf(algorithm.name()));
            return digester.digest(data);
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Digest failed, algorithm: " + algorithm, e);
        }
    }

    @Override
    public byte[] digest(byte[] data) {
        return digest(data, DigestAlgorithm.SHA256);
    }
}
