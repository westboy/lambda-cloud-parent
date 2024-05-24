package com.jingfang.cloud.mybatis.encrypt;

import cn.hutool.core.util.StrUtil;
import com.jingfang.cloud.mybatis.kit.AesKit;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author jpjoo
 */
public class AesEncryptHandler extends BaseTypeHandler<Object> {

    public static String key = "AD42F697B035CKU9";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, AesKit.AES_ENCODER.encryptForAes((String) parameter, key));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        String value = AesKit.AES_DECODER.decryptForAesToStr(columnValue, key);
        return StrUtil.isNotEmpty(value) ? value : columnValue;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String columnValue = rs.getString(columnIndex);
        String value = AesKit.AES_DECODER.decryptForAesToStr(columnValue, key);
        return StrUtil.isNotEmpty(value) ? value : columnValue;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String columnValue = cs.getString(columnIndex);
        return AesKit.AES_DECODER.decryptForAesToStr(columnValue, key);
    }

    public static void main(String[] args) {
        String name1 = AesKit.AES_DECODER.decryptForAesToStr("5KrQY2mzMer32IOwddkkDQ==", key);
        String value1 = AesKit.AES_DECODER.decryptForAesToStr("IbgboXFvhlOjNoyuYA+xq4ob24q/dVLo0Pujz2+9CfA=", key);
    }
}
