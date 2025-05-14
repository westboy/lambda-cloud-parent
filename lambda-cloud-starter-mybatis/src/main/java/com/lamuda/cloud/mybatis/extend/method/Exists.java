package com.lambda.cloud.mybatis.extend.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * Exists
 *
 * @author jpjoo
 */
public class Exists extends AbstractMethod {

    public Exists() {
        super("exists");
    }

    /**
     * @param name 方法名
     * @since 3.5.0
     */
    public Exists(String name) {
        super(name);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sql = String.format("<script>SELECT COUNT(1) FROM DUAL WHERE EXISTS (SELECT 1 FROM %s %s %s)</script>",
                tableInfo.getTableName(), sqlWhereEntityWrapper(true, tableInfo), sqlComment());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForOther(mapperClass, methodName, sqlSource, boolean.class);
    }
}
