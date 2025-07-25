package com.lambda.cloud.mybatis.mapping;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author Jin
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public record LambdaSqlSource(BoundSql boundSql) implements SqlSource {

    public LambdaSqlSource(BoundSql boundSql) {
        this.boundSql = boundSql;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return boundSql;
    }
}
