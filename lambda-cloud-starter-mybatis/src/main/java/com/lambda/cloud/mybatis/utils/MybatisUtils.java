package com.lambda.cloud.mybatis.utils;

import com.lambda.cloud.mybatis.mapping.BoundSql;
import com.lambda.cloud.mybatis.mapping.SqlSource;
import org.apache.commons.lang.ArrayUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.DOT;

/**
 * @author Jin
 */
public final class MybatisUtils {

    private MybatisUtils() {
    }

    /**
     * 构造新的MappedStatement
     *
     * @param statement
     * @param parameterObject
     * @param sql
     * @return org.apache.ibatis.mapping.MappedStatement
     */
    public static MappedStatement newMappedStatement(MappedStatement statement, Object parameterObject, String sql) {
        org.apache.ibatis.mapping.BoundSql boundSql = statement.getBoundSql(parameterObject);
        return newMappedStatement(statement, boundSql, sql);
    }

    /**
     * 构造新的MappedStatement
     *
     * @param statement
     * @param boundSql
     * @param sql
     * @return org.apache.ibatis.mapping.MappedStatement
     */
    public static MappedStatement newMappedStatement(MappedStatement statement, org.apache.ibatis.mapping.BoundSql boundSql, String sql) {
        org.apache.ibatis.mapping.SqlSource sqlSource = newSqlSource(modifyBoundSql(boundSql, sql));
        return MybatisUtils.newMappedStatement(statement, sqlSource);
    }

    /**
     * 构造新的MappedStatement
     *
     * @param statement 原始SQL节点
     * @param sqlsource 新的内容
     * @return org.apache.ibatis.mapping.MappedStatement
     */
    public static MappedStatement newMappedStatement(MappedStatement statement, org.apache.ibatis.mapping.SqlSource sqlsource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(statement.getConfiguration(), statement.getId()
                , sqlsource, statement.getSqlCommandType());
        builder.resource(statement.getResource());
        builder.fetchSize(statement.getFetchSize());
        builder.statementType(statement.getStatementType());
        builder.keyGenerator(statement.getKeyGenerator());
        if (ArrayUtils.isNotEmpty(statement.getKeyProperties())) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : statement.getKeyProperties()) {
                keyProperties.append(keyProperty).append(DOT);
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(statement.getTimeout());
        builder.parameterMap(statement.getParameterMap());
        builder.resultMaps(statement.getResultMaps());
        builder.resultSetType(statement.getResultSetType());
        builder.cache(statement.getCache());
        builder.flushCacheRequired(statement.isFlushCacheRequired());
        builder.useCache(statement.isUseCache());
        return builder.build();
    }

    /**
     * 构造新的SqlSource
     *
     * @param boundSql
     * @return org.apache.ibatis.mapping.SqlSource
     */
    public static org.apache.ibatis.mapping.SqlSource newSqlSource(org.apache.ibatis.mapping.BoundSql boundSql) {
        return new SqlSource(boundSql);
    }

    /**
     * 构造新的BoundSql
     *
     * @param configuration
     * @param source
     * @param sql
     * @return org.apache.ibatis.mapping.BoundSql
     */
    public static org.apache.ibatis.mapping.BoundSql newBoundSql(Configuration configuration, org.apache.ibatis.mapping.BoundSql source, String sql) {
        return new BoundSql(configuration, source, sql);
    }

    /***
     * 重写SQL
     * @param source
     * @param sql
     * @return org.apache.ibatis.mapping.BoundSql
     */
    public static org.apache.ibatis.mapping.BoundSql modifyBoundSql(org.apache.ibatis.mapping.BoundSql source, String sql) {
        MetaObject object = SystemMetaObject.forObject(source);
        object.setValue("sql", sql);
        return source;
    }

    /**
     * 获取当前拦截的方法
     *
     * @param mappedStatement
     * @return java.lang.reflect.Method
     */
    @Nullable
    public static Method getCurrentMethod(@Nonnull MappedStatement mappedStatement) {
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf(DOT));
        String methodName = id.substring(id.lastIndexOf(DOT) + 1);
        try {
            final Class<?> cls = Resources.classForName(className);
            final Method[] methods = cls.getMethods();
            return Arrays.stream(methods).filter(item -> item.getName().equals(methodName)).findFirst().orElse(null);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }
}
