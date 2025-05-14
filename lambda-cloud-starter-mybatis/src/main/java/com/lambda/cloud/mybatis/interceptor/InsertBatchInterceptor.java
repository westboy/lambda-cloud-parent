package com.lambda.cloud.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.lambda.cloud.mybatis.extend.ExtendBaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;

/**
 * 批量插入 拦截器
 *
 * @author jpjoo
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class InsertBatchInterceptor implements Interceptor {

    private static final String COMMON_INSERT_BATCH_METHOD = ".insertAll";
    private static final String MYSQL_INSERT_BATCH_METHOD = "mysqlInsertAllBatch";
    private static final String ORACLE_INSERT_BATCH_METHOD = "oracleInsertAllBatch";

    /**
     * 替换批量执行方法为 对应数据库的语法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        String method = ms.getId();
        String mapperName = method.substring(0, method.lastIndexOf(Constants.DOT));
        Class<?> clazz = Resources.classForName(mapperName);
        if (ExtendBaseMapper.class.isAssignableFrom(clazz) && method.endsWith(COMMON_INSERT_BATCH_METHOD)) {
            Configuration configuration = ms.getConfiguration();
            DbType dbType = JdbcUtils.getDbType(((Executor) invocation.getTarget()));
            String methodName;
            switch (dbType) {
                case ORACLE:
                case ORACLE_12C:
                    methodName = ORACLE_INSERT_BATCH_METHOD;
                    break;
                case MARIADB:
                case MYSQL:
                    methodName = MYSQL_INSERT_BATCH_METHOD;
                    break;
                default:
                    methodName = MYSQL_INSERT_BATCH_METHOD;
                    log.info("暂不支持的数据库类型{}", ms.getId());
            }
            args[0] = configuration.getMappedStatement(mapperName + Constants.DOT + methodName);
            log.trace("批量插入执行方法为{}", ms.getId());
        }
        return invocation.proceed();
    }
}
