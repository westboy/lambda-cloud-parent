package com.lamuda.cloud.mybatis.extend.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import javax.annotation.Nonnull;

/**
 * UpdateByCode
 *
 * @author jpjoo
 */
public class UpdateByCode extends AbstractMethod implements CurdByCode {

    private final TableFieldInfo codeField;

    public UpdateByCode(TableFieldInfo codeField) {
        super("updateByCode");
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
        final String sql = "<script>\nUPDATE %s %s WHERE %s %s\n</script>";
        final String additional = optlockVersion(tableInfo) + tableInfo.getLogicDeleteSql(true, true);
        String updateSql = String.format(sql, tableInfo.getTableName(),
                sqlSet(tableInfo.isWithLogicDelete(), false, tableInfo, false, ENTITY, ENTITY_DOT),
                codeSql(codeField), additional);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, updateSql, modelClass);
        return addUpdateMappedStatement(mapperClass, modelClass, this.methodName, sqlSource);
    }

    /**
     * 根据 TableCodeField 注解  获取 code字段
     *
     * @return 对应的 code 查询sql  如    ‘code = #{ew.code}’
     */
    @Override
    public String codeSql(@Nonnull TableFieldInfo codeField) {
        return codeField.getColumn() + " = #{" + ENTITY_DOT + codeField.getProperty() + "}";
    }
}
