package com.lambda.cloud.mybatis.datascope;

import static com.lambda.cloud.mybatis.datascope.support.DataScopeEvaluator.*;
import static com.lambda.cloud.mybatis.utils.MappedStatementUtils.getCurrentMethod;
import static com.lambda.cloud.mybatis.utils.MappedStatementUtils.newMappedStatement;

import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Sets;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.datascope.annotation.DataScope;
import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import com.lambda.cloud.mybatis.datascope.strategy.DataScopeStrategy;
import com.lambda.cloud.mybatis.datascope.support.DataScopeEvaluationContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.ClassUtils;

/**
 * 数据权限拦截器
 *
 * @author Jin
 */
@Slf4j
@Intercepts({
    @Signature(
            type = Executor.class,
            method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public record DataScopeInterceptor(Map<Integer, Integer> typeMapper) implements Interceptor {

    private static final String DATA_SCOPE_MS_ID = "dataScopeMappedStatementId";
    private static final int MAX = 1000;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Executor executor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];

        if (!SqlCommandType.SELECT.equals(statement.getSqlCommandType())) {
            return invocation.proceed();
        }
        Method method = getCurrentMethod(statement);
        BoundSql boundSql = statement.getBoundSql(parameter);
        String sql = boundSql.getSql();
        DataScopeContext context = buildDataScopeContext(method, sql);
        if (context == null) {
            return invocation.proceed();
        }
        // 处理type映射
        if (typeMapper != null && !typeMapper.isEmpty()) {
            context.setType(Arrays.stream(context.getType())
                    .map(type -> {
                        if (typeMapper.containsKey(type)) {
                            return typeMapper.get(type);
                        }
                        return type;
                    })
                    .toArray());
        }

        boolean replace = getReplace(sql);
        if (replace) {
            context.setMode(DataScope.Mode.SUB_QUERY);
            context.setReplace(true);
            log.debug(
                    "It is detected that the SQL contains data permission flags, and the subquery mode is forced to be used.");
        }
        LoginUser operator = getOperator(parameter);
        if (operator == null) {
            log.warn("When using @DataScope, the user must be provided. Otherwise it will be ignored.");
            return invocation.proceed();
        }
        boolean owner = isOwner(operator);
        String updated;
        if (owner) {
            if (replace) {
                updated = modifySqlForOwner(sql);
            } else {
                return invocation.proceed();
            }
        } else {
            Set<String> permissions = Collections.emptySet();
            if (context.isPretreatment()) {
                permissions = getUserPermissions(executor, statement, rowBounds, context, operator);
                // 当用户无数据权限时，直接返回相应的结果
                if (CollectionUtils.isEmpty(permissions)) {
                    return emptyResult(method);
                }
            }
            DataScopeEvaluationContext dataScopeEvaluationContext =
                    new DataScopeEvaluationContext(operator, context, permissions);
            DataScopeStrategy strategy = DataScopeStrategy.getInstance(context.getMode());
            updated = strategy.improve(sql, dataScopeEvaluationContext);
        }
        args[0] = newMappedStatement(statement, boundSql, updated);
        return invocation.proceed();
    }

    /**
     * 根据方法返回空结果集
     *
     * @return java.lang.Object
     */
    private Object emptyResult(Method method) {
        if (method != null) {
            Class<?> returnType = method.getReturnType();
            if (ClassUtils.isAssignable(Integer.class, returnType) || ClassUtils.isAssignable(int.class, returnType)) {
                return 0;
            } else if (ClassUtils.isAssignable(Long.class, returnType)
                    || ClassUtils.isAssignable(long.class, returnType)) {
                return 0L;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 预获取用户权限
     *
     * @return java.util.Set<java.lang.String>
     */
    private Set<String> getUserPermissions(
            Executor executor,
            MappedStatement statement,
            RowBounds rowBounds,
            DataScopeContext context,
            LoginUser operator)
            throws SQLException {
        final Configuration configuration = statement.getConfiguration();
        MappedStatement pms = buildDataScopeMappedStatement(configuration, context, operator);
        List<String> result = executor.query(pms, null, rowBounds, null);
        Set<String> permissions = Sets.newHashSet(result);
        if (permissions.size() > MAX) {
            log.warn(
                    "The user has too many permissions, an exception may occur during execution! size: {}",
                    permissions.size());
        }
        return permissions;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    private static MappedStatement buildDataScopeMappedStatement(
            @Nonnull Configuration configuration, @Nonnull DataScopeContext context, @Nonnull LoginUser operator) {
        String sql = buildSQL01(context, operator);
        SqlSource sqlSource = new StaticSqlSource(configuration, sql);
        MappedStatement.Builder builder =
                new MappedStatement.Builder(configuration, DATA_SCOPE_MS_ID, sqlSource, SqlCommandType.SELECT);
        builder.resultSetType(ResultSetType.DEFAULT);
        builder.statementType(StatementType.PREPARED);
        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(
                new ResultMap.Builder(configuration, IdUtil.nanoId(8), String.class, Collections.emptyList()).build());
        builder.resultMaps(resultMaps);
        return builder.build();
    }

    /**
     * 获取数据权限注解信息
     *
     * @param method 当前方法
     */
    private static DataScopeContext buildDataScopeContext(Method method, String sql) {
        if (method != null) {
            DataScope actual = method.getAnnotation(DataScope.class);
            if (Objects.nonNull(actual)) {
                DataScopeContext context = new DataScopeContext();
                context.setKey(actual.key());
                context.setLevel(actual.level());
                context.setLevelExp(actual.levelExp());
                context.setType(actual.type());
                context.setMode(actual.mode());
                context.setScheme(actual.scheme());
                context.setCondition(actual.condition());
                context.setPretreatment(actual.pretreatment());
                context.setChecked(actual.checked());
                return context;
            }
        }
        return null;
    }
}
