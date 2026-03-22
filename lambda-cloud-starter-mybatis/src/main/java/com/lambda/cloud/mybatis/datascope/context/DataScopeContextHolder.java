package com.lambda.cloud.mybatis.datascope.context;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

import cn.hutool.core.lang.Assert;
import com.lambda.cloud.mybatis.datascope.annotation.DataScope;

/**
 * 动态数据权限管理器
 *
 * @author Jin
 */
public final class DataScopeContextHolder implements AutoCloseable {

    private static final ThreadLocal<DataScopeContext> CONTEXT = new ThreadLocal<>();

    private static final DataScopeContextHolder HELPER = new DataScopeContextHolder();

    private DataScopeContextHolder() {}

    public static DataScopeContextHolder getInstance() {
        return HELPER;
    }

    public DataScopeContext getDataScopeContext() {
        return CONTEXT.get();
    }

    public static void setDataScopeContext(int[] type, String key) {
        setDataScopeContext(type, key, Integer.MAX_VALUE, DataScope.Mode.INNER, DataScope.Scheme.CASCADE);
    }

    public static void setDataScopeContext(int[] type, String key, int level) {
        setDataScopeContext(type, key, level, DataScope.Mode.INNER, DataScope.Scheme.CASCADE);
    }

    public static void setDataScopeContext(int[] type, String key, int level, DataScope.Mode mode) {
        setDataScopeContext(type, key, level, mode, DataScope.Scheme.CASCADE);
    }

    public static void setDataScopeContext(
            int[] type, String key, int level, DataScope.Mode mode, DataScope.Scheme scheme) {
        DataScopeContext dataScopeContext = new DataScopeContext();
        dataScopeContext.setType(type);
        dataScopeContext.setKey(key);
        dataScopeContext.setLevel(level);
        dataScopeContext.setLevelExp(DataScope.Expression.EQ);
        dataScopeContext.setMode(mode);
        dataScopeContext.setScheme(scheme);
        dataScopeContext.setCondition(EMPTY);
        dataScopeContext.setPretreatment(false);
        setDataScopeContext(dataScopeContext);
    }

    public static void setDataScopeContext(DataScopeContext dataScopeContext) {
        Assert.notNull(dataScopeContext.getType(), "DataScope type can not be null!");
        Assert.notNull(dataScopeContext.getKey(), "DataScope key can not be null!");
        CONTEXT.set(dataScopeContext);
    }

    @Override
    public void close() {
        CONTEXT.remove();
    }
}
