package com.lambda.cloud.mybatis.injector.method;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * InsertAll
 *
 * @author Jin
 */
@SuppressWarnings("all")
public class InsertAll extends AbstractInsertBatch {

    public InsertAll() {
        super("insertAll");
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String databaseId = configuration.getDatabaseId();
        if ("oracle".equalsIgnoreCase(databaseId)) {
            return injectOracleCompatible(mapperClass, modelClass, tableInfo);
        } else {
            return injectStandard(mapperClass, modelClass, tableInfo);
        }
    }
}
