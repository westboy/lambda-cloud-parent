package com.lamuda.cloud.mybatis.support;

import org.apache.ibatis.mapping.BoundSql;

/**
 * @author Jin
 */
public class SqlSource implements org.apache.ibatis.mapping.SqlSource {

    private final BoundSql boundSql;

    public SqlSource(BoundSql boundSql) {
        this.boundSql = boundSql;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return boundSql;
    }

}