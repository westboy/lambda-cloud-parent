package com.lambda.cloud.mybatis.injector.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;


/**
 * SelectByCode
 *
 * @author jpjoo
 */
public class SelectByCode extends AbstractMethod implements CurdByCode {

    private final TableFieldInfo codeField;

    public SelectByCode(TableFieldInfo codeField) {
        super("selectByCode");
        this.codeField = codeField;

    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final String sql = " SELECT %s FROM %s WHERE %s ";
        String selectSql = String.format(sql,
                this.sqlSelectColumns(tableInfo, false),
                tableInfo.getTableName(),
                codeSql(codeField));
        SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, selectSql, modelClass);
        return this.addSelectMappedStatementForTable(mapperClass, this.methodName, sqlSource, tableInfo);
    }
}
