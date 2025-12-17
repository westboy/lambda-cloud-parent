package com.lambda.cloud.mybatis.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.AES;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * @author jpjoo
 */
@Setter
@Slf4j
public class AesEncryptHandler extends BaseTypeHandler<String> {

    public final String key;

    public AesEncryptHandler(String key) {
        this.key = key;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, AES.encrypt(parameter, key));
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
        String value = AES.decrypt(columnValue, key);
        if (StrUtil.isEmpty(value)) {
            log.warn("AES decryption failed or returned empty for value. Returning null.");
            return null;
        }
        return value;
    }
}
