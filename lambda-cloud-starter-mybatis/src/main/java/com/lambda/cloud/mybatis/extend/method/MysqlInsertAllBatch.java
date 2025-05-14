package com.lambda.cloud.mybatis.extend.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * MysqlInsertAllBatch
 * @author jpjoo
 */
@SuppressWarnings("all")
public class MysqlInsertAllBatch extends AbstractMethod {
    /**
     * @param methodName 方法名
     * @since 3.5.0
     */
    public MysqlInsertAllBatch() {
        super("mysqlInsertAllBatch");
    }

    /**
     * 注入自定义 MappedStatement
     *
     * @param mapperClass mapper 接口
     * @param modelClass  mapper 泛型
     * @param tableInfo   数据库表反射信息
     * @return MappedStatement
     */
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final String sql = "<script>insert into %s %s values %s</script>";
        final String fieldSql = prepareFieldSql(tableInfo);
        final String valueSql = prepareValuesSqlForMysqlBatch(tableInfo);
        final String sqlResult = String.format(sql, tableInfo.getTableName(), fieldSql, valueSql);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlResult, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, "mysqlInsertAllBatch", sqlSource, new NoKeyGenerator(), null, null);
    }

    private String prepareFieldSql(TableInfo tableInfo) {
        StringBuilder fieldSql = new StringBuilder();
        String primaryKey = tableInfo.getKeyColumn();
        if (StringUtils.isNotBlank(primaryKey)) {
            fieldSql.append(tableInfo.getKeyColumn()).append(COMMA);
        }
        tableInfo.getFieldList().forEach(x -> fieldSql.append(x.getColumn()).append(COMMA));
        fieldSql.delete(fieldSql.length() - 1, fieldSql.length());
        fieldSql.insert(0, LEFT_BRACKET);
        fieldSql.append(RIGHT_BRACKET);
        return fieldSql.toString();
    }

    private String prepareValuesSqlForMysqlBatch(TableInfo tableInfo) {
        final StringBuilder valueSql = new StringBuilder();
        valueSql.append("<foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"),(\" close=\")\">");
        String primaryKey = tableInfo.getKeyProperty();
        if (StringUtils.isNotBlank(primaryKey)) {
            valueSql.append("#{item.").append(tableInfo.getKeyProperty()).append(RIGHT_BRACE).append(COMMA);
        }
        tableInfo.getFieldList().forEach(x -> valueSql.append("#{item.").append(x.getProperty()).append(RIGHT_BRACE).append(COMMA));
        valueSql.delete(valueSql.length() - 1, valueSql.length());
        valueSql.append("</foreach>");
        return valueSql.toString();
    }
}

