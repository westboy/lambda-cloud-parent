package com.lambda.cloud.mybatis.interceptor;

import com.lambda.cloud.core.convert.TypeConverter;
import com.lambda.cloud.core.utils.OperatorUtils;
import com.lambda.cloud.mybatis.tenant.TenantContextHolder;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;

/**
 * 租户拦截拦截器
 *
 * @author Jin
 */
@SuppressWarnings("unchecked")
@Slf4j
@Intercepts({
    @Signature(
            type = Executor.class,
            method = "update",
            args = {MappedStatement.class, Object.class}),
    @Signature(
            type = Executor.class,
            method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(
            type = Executor.class,
            method = "query",
            args = {
                MappedStatement.class,
                Object.class,
                RowBounds.class,
                ResultHandler.class,
                CacheKey.class,
                BoundSql.class
            })
})
public record TenantExpressionInterceptor(String name) implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String tenantid;
        Object[] args = invocation.getArgs();
        Object parameter = args[1];
        if (parameter instanceof MapperMethod.ParamMap) {
            tenantid = getTenantFromMap((MapperMethod.ParamMap<Object>) parameter);
        } else {
            tenantid = getTenantFromBean(parameter);
        }
        if (tenantid == null) {
            tenantid = getTenantFromUser();
        }

        if (StringUtils.isBlank(tenantid)) {
            return invocation.proceed();
        }
        try (TenantContextHolder tenantManager = TenantContextHolder.getInstance()) {
            tenantManager.setTenantId(tenantid);
            return invocation.proceed();
        }
    }

    private String getTenantFromMap(MapperMethod.ParamMap<Object> parameter) {
        if (parameter.containsKey(name)) {
            Object tenant = parameter.get(name);
            if (tenant != null) {
                return (String) TypeConverter.convert(tenant, String.class);
            }
        }
        return null;
    }

    private String getTenantFromBean(Object parameter) {
        try {
            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(parameter.getClass(), name);
            if (descriptor != null) {
                Method method = descriptor.getReadMethod();
                Object tenant = method.invoke(parameter);
                if (tenant != null) {
                    return (String) TypeConverter.convert(tenant, String.class);
                }
            }
        } catch (Exception exception) {
            log.error("get tenant from bean error", exception);
        }
        return null;
    }

    private String getTenantFromUser() {
        return OperatorUtils.getOperator().getTenantId();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }
}
