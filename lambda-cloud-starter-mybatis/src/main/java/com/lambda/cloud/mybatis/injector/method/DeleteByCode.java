package com.lambda.cloud.mybatis.injector.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;


/**
 * DeleteByCode
 *
 * @author jpjoo
 */
public class DeleteByCode extends AbstractMethod implements CurdByCode {

    private final TableFieldInfo codeField;

    public DeleteByCode(TableFieldInfo codeField) {
        super("deleteByCode");
        this.codeField = codeField;
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
        String sql;
        final String logicDeleteSql = "<script>\nUPDATE %s %s WHERE %s %s \n</script>";
        final String deleteSql = "<script>\nDELETE FROM %s WHERE %s \n</script>";
        if (tableInfo.isWithLogicDelete()) {
            sql = String.format(logicDeleteSql, tableInfo.getTableName(), sqlLogicSet(tableInfo),
                    this.codeSql(codeField),
                    tableInfo.getLogicDeleteSql(true, true));
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Object.class);
            return addUpdateMappedStatement(mapperClass, modelClass, this.methodName, sqlSource);
        } else {
            sql = String.format(deleteSql, tableInfo.getTableName(),
                    this.codeSql(codeField));
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Object.class);
            return this.addDeleteMappedStatement(mapperClass, this.methodName, sqlSource);
        }
    }


}
