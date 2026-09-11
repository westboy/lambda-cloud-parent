package com.lambda.security.web.form;

/**
 * 登录凭据解密器扩展点
 * <p>
 * 启用凭据传输加密（{@code lambda.security.form.credentials-encrypt.enabled=true}）后，
 * 表单登录提交的 username/password 为密文，过滤器在参数提取后调用本接口解密。
 * 默认实现基于 {@code lambda-cloud-starter-crypto} 的非对称加解密（RSA-OAEP / SM2）；
 * 下游可注册自定义 Bean 覆盖（如对接自有加密协议）。
 *
 * @author Jin
 * @since 2026.1.1
 */
public interface CredentialsDecryptor {

    /**
     * 解密登录凭据字段
     *
     * @param encrypted 密文（前端约定编码），空白时原样返回
     * @return 明文
     * @throws com.lambda.security.exception.AuthenticationException 解密失败时抛出
     */
    String decrypt(String encrypted);
}
