package com.lambda.cloud.mybatis.injector.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 批量插入基类
 *
 * @author Jin
 */
public abstract class AbstractInsertBatch extends AbstractMethod {

    protected AbstractInsertBatch(String methodName) {
        super(methodName);
    }

    /**
     * 注入标准 SQL 批量插入语句 (适用于 MySQL, PostgreSQL, SQL Server, H2, DB2 等)
     * 使用 VALUES (r1), (r2) 语法
     */
    protected MappedStatement injectStandard(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final String sql = "<script>insert into %s %s values %s</script>";
        final String fieldSql = prepareFieldSql(tableInfo);
        final String valueSql = prepareValuesSqlForStandardBatch(tableInfo);
        final String sqlResult = String.format(sql, tableInfo.getTableName(), fieldSql, valueSql);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlResult, modelClass);
        return this.addInsertMappedStatement(
                mapperClass, modelClass, this.methodName, sqlSource, new NoKeyGenerator(), null, null);
    }

    /**
     * 注入 Oracle 兼容的批量插入语句 (适用于 Oracle, 达梦 DM 等)
     * 使用 INSERT ALL ... SELECT 1 FROM DUAL 语法
     */
    protected MappedStatement injectOracleCompatible(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final String sql = "<script>insert all %s select 1 from dual</script>";
        final String valueSql = prepareValuesSqlForOracleBatch(tableInfo);
        final String sqlResult = String.format(sql, valueSql);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlResult, modelClass);
        return this.addInsertMappedStatement(
                mapperClass, modelClass, this.methodName, sqlSource, new NoKeyGenerator(), null, null);
    }

    protected String prepareFieldSql(TableInfo tableInfo) {
        StringBuilder fieldSql = new StringBuilder();
        fieldSql.append(tableInfo.getKeyColumn()).append(StringPool.COMMA);
        tableInfo.getFieldList().forEach(x -> fieldSql.append(x.getColumn()).append(StringPool.COMMA));
        fieldSql.delete(fieldSql.length() - 1, fieldSql.length());
        fieldSql.insert(0, StringPool.LEFT_BRACKET);
        fieldSql.append(StringPool.RIGHT_BRACKET);
        return fieldSql.toString();
    }

    protected String prepareValuesSqlForStandardBatch(TableInfo tableInfo) {
        final StringBuilder valueSql = new StringBuilder();
        valueSql.append(
                "<foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"),(\" close=\")\">");
        valueSql.append("#{item.").append(tableInfo.getKeyProperty()).append("},");
        tableInfo
                .getFieldList()
                .forEach(x -> valueSql.append("#{item.").append(x.getProperty()).append("},"));
        valueSql.delete(valueSql.length() - 1, valueSql.length());
        valueSql.append("</foreach>");
        return valueSql.toString();
    }

    protected String prepareValuesSqlForOracleBatch(TableInfo tableInfo) {
        final String tableName = tableInfo.getTableName();
        final String fieldSql = prepareFieldSql(tableInfo);
        final StringBuilder valueSql = new StringBuilder();
        valueSql.append("<foreach collection=\"list\" item=\"item\" index=\"index\">");
        valueSql.append(" INTO ").append(tableName).append(fieldSql).append(" VALUES (");
        final String primaryKey = tableInfo.getKeyProperty();
        if (StringUtils.isNotBlank(primaryKey)) {
            valueSql.append("#{item.").append(tableInfo.getKeyProperty()).append("},");
        }
        tableInfo
                .getFieldList()
                .forEach(x -> valueSql.append("#{item.").append(x.getProperty()).append("},"));
        valueSql.delete(valueSql.length() - 1, valueSql.length());
        valueSql.append(StringPool.RIGHT_BRACKET);
        valueSql.append("</foreach>");
        return valueSql.toString();
    }
}
