package com.lambda.cloud.mybatis.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.AES;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.Setter;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * @author jpjoo
 */
@Setter
public class AesEncryptHandler extends BaseTypeHandler<Object> {

    public final String key;

    public AesEncryptHandler(String key) {
        this.key = key;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, AES.encrypt((String) parameter, key));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        if (StrUtil.isEmpty(columnValue)) {
            return columnValue;
        }
        String value = AES.decrypt(columnValue, key);
        if (StrUtil.isEmpty(value)) {
            throw new SQLException("AES decryption failed for column: " + columnName);
        }
        return value;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String columnValue = rs.getString(columnIndex);
        if (StrUtil.isEmpty(columnValue)) {
            return columnValue;
        }
        String value = AES.decrypt(columnValue, key);
        if (StrUtil.isEmpty(value)) {
            throw new SQLException("AES decryption failed for column index: " + columnIndex);
        }
        return value;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String columnValue = getColumnValue(cs, columnIndex);
        if (StrUtil.isEmpty(columnValue)) {
            return columnValue;
        }
        String value = AES.decrypt(columnValue, key);
        if (StrUtil.isEmpty(value)) {
            throw new SQLException("AES decryption failed for column index: " + columnIndex);
        }
        return value;
    }

    private String getColumnValue(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }
}
