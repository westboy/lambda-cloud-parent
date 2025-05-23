package com.lambda.cloud.mybatis.interceptor;

import static com.lambda.cloud.mybatis.purview.utils.PurviewUtils.*;
import static com.lambda.cloud.mybatis.utils.MybatisUtils.getCurrentMethod;
import static com.lambda.cloud.mybatis.utils.MybatisUtils.newMappedStatement;

import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.purview.annotation.Purview;
import com.lambda.cloud.mybatis.purview.annotation.PurviewModeStrategy;
import com.lambda.cloud.mybatis.purview.support.DynamicPurview;
import com.lambda.cloud.mybatis.purview.support.Parameters;
import com.lambda.cloud.mybatis.purview.utils.PurviewUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Method;
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
public class PurviewInterceptor implements Interceptor {

    private static final String PURVIEW_MS_ID = "purviewMappedStatementId";
    private static final int MAX = 1000;

    @SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
    private final Map<Integer, Integer> typeMapper;

    public PurviewInterceptor(Map<Integer, Integer> typeMapper) {
        this.typeMapper = typeMapper;
    }

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
        DynamicPurview purview = getDynamicPurview(method, sql);
        if (purview == null) {
            return invocation.proceed();
        }
        // 处理type映射
        if (typeMapper != null && !typeMapper.isEmpty()) {
            purview.setType(Arrays.stream(purview.getType())
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
            purview.setMode(Purview.Mode.SUB_QUERY);
            purview.setReplace(true);
            log.debug(
                    "It is detected that the SQL contains data permission flags, and the subquery mode is forced to be used.");
        }
        LoginUser operator = getOperator(parameter);
        if (operator == null) {
            log.warn("When using @Purview, the user must be provided. Otherwise it will be ignored.");
            return invocation.proceed();
        }
        boolean owner = PurviewUtils.isOwner(operator);
        String updated;
        if (owner) {
            if (replace) {
                updated = PurviewUtils.modifySqlForOwner(sql);
            } else {
                return invocation.proceed();
            }
        } else {
            Set<String> permissions = Collections.emptySet();
            if (purview.isPretreatment()) {
                permissions = getUserPermissions(executor, statement, rowBounds, purview, operator);
                // 当用户无数据权限时，直接返回相应的结果
                if (CollectionUtils.isEmpty(permissions)) {
                    return emptyResult(method);
                }
            }
            Parameters parameters = new Parameters(operator, purview, permissions);
            PurviewModeStrategy strategy = PurviewModeStrategy.getInstance(purview.getMode());
            updated = strategy.improve(sql, parameters);
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
        if (method != null && ClassUtils.isAssignable(method.getReturnType(), Integer.class)) {
            return Lists.newArrayList(0);
        } else {
            return Collections.emptyList();
        }
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
            DynamicPurview purview,
            LoginUser operator)
            throws java.sql.SQLException {
        final Configuration configuration = statement.getConfiguration();
        MappedStatement pms = buildPurviewMappedStatement(configuration, purview, operator);
        List<String> result = executor.query(pms, null, rowBounds, null);
        Set<String> permissions = Sets.newHashSet(result);
        if (permissions.size() > MAX) {
            log.warn(
                    "The user has too many permissions, an exception may occur during execution! size: {}",
                    permissions.size());
        }
        return permissions;
    }

    private MappedStatement buildPurviewMappedStatement(
            @Nonnull Configuration configuration, @Nonnull DynamicPurview purview, @Nonnull LoginUser operator) {
        String sql = PurviewUtils.buildSQL01(purview, operator);
        SqlSource sqlSource = new StaticSqlSource(configuration, sql);
        MappedStatement.Builder builder =
                new MappedStatement.Builder(configuration, PURVIEW_MS_ID, sqlSource, SqlCommandType.SELECT);
        builder.resultSetType(ResultSetType.DEFAULT);
        builder.statementType(StatementType.PREPARED);
        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(
                new ResultMap.Builder(configuration, IdUtil.nanoId(8), String.class, Collections.emptyList()).build());
        builder.resultMaps(resultMaps);
        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }
}
