package com.lambda.cloud.mybatis.handler;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.crypto.service.SymmetricCryptoService;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * 敏感字段加解密 TypeHandler
 * <p>
 * 委托 lambda-cloud-starter-crypto 的 {@link SymmetricCryptoService} 实现：
 * AES-GCM / SM4-GCM，随机 IV 前置，密钥由 {@code lambda.crypto.keys} 统一管理。
 * 数据库列存储 Base64 编码的密文（{@code Base64(iv || ciphertext)}）。
 *
 * @author jpjoo
 */
@Setter
@Slf4j
public class AesEncryptHandler extends BaseTypeHandler<String> {

    private final SymmetricCryptoService symmetricCryptoService;

    /**
     * 密钥标识，对应 {@code lambda.crypto.keys} 中的 {@code id}
     */
    private final String keyId;

    public AesEncryptHandler(SymmetricCryptoService symmetricCryptoService, String keyId) {
        this.symmetricCryptoService = symmetricCryptoService;
        this.keyId = keyId;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        byte[] encrypted = symmetricCryptoService.encrypt(parameter.getBytes(StandardCharsets.UTF_8), keyId);
        ps.setString(i, Base64.encode(encrypted));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        return decrypt(columnValue);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String columnValue = rs.getString(columnIndex);
        return decrypt(columnValue);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String columnValue = cs.getString(columnIndex);
        return decrypt(columnValue);
    }

    private String decrypt(String columnValue) {
        if (StrUtil.isEmpty(columnValue)) {
            return columnValue;
        }
        byte[] decrypted = symmetricCryptoService.decrypt(Base64.decode(columnValue), keyId);
        String value = new String(decrypted, StandardCharsets.UTF_8);
        if (StrUtil.isEmpty(value)) {
            log.warn("AES decryption failed or returned empty for value. Returning null.");
            return null;
        }
        return value;
    }
}
