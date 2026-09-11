package com.lambda.security.web.form;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.crypto.service.AsymmetricCryptoService;
import com.lambda.security.exception.AuthenticationException;
import java.nio.charset.StandardCharsets;

/**
 * 基于 lambda-cloud-starter-crypto 的登录凭据解密默认实现
 * <p>
 * 前端约定：明文经非对称公钥加密（RSA-OAEP / SM2）后 Base64 编码提交。
 * 解密失败抛通用认证异常，不区分具体原因，日志不输出密文原文。
 *
 * @author Jin
 * @since 2026.1.1
 */
public class AsymmetricCredentialsDecryptor implements CredentialsDecryptor {

    private final AsymmetricCryptoService asymmetricCryptoService;

    private final String keyId;

    public AsymmetricCredentialsDecryptor(AsymmetricCryptoService asymmetricCryptoService, String keyId) {
        this.asymmetricCryptoService = asymmetricCryptoService;
        this.keyId = keyId;
    }

    @Override
    public String decrypt(String encrypted) {
        if (StrUtil.isBlank(encrypted)) {
            return encrypted;
        }
        try {
            return new String(asymmetricCryptoService.decrypt(Base64.decode(encrypted), keyId), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AuthenticationException("登录凭据解密失败");
        }
    }
}
