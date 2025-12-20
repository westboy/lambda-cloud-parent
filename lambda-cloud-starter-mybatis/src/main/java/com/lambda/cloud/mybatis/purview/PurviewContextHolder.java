package com.lambda.cloud.mybatis.purview;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

import cn.hutool.core.lang.Assert;
import com.lambda.cloud.mybatis.purview.annotation.Purview;

/**
 * 动态数据权限管理器
 *
 * @author Jin
 */
public final class PurviewContextHolder implements AutoCloseable {

    private static final ThreadLocal<PurviewContext> CONTEXT = new ThreadLocal<>();

    private static final PurviewContextHolder HELPER = new PurviewContextHolder();

    private PurviewContextHolder() {}

    public static PurviewContextHolder getInstance() {
        return HELPER;
    }

    public PurviewContext getPurview() {
        return CONTEXT.get();
    }

    public static void setPurview(int[] type, String key) {
        setPurview(type, key, Integer.MAX_VALUE, Purview.Mode.INNER, Purview.Scheme.CASCADE);
    }

    public static void setPurview(int[] type, String key, int level) {
        setPurview(type, key, level, Purview.Mode.INNER, Purview.Scheme.CASCADE);
    }

    public static void setPurview(int[] type, String key, int level, Purview.Mode mode) {
        setPurview(type, key, level, mode, Purview.Scheme.CASCADE);
    }

    public static void setPurview(int[] type, String key, int level, Purview.Mode mode, Purview.Scheme scheme) {
        PurviewContext purview = new PurviewContext();
        purview.setType(type);
        purview.setKey(key);
        purview.setLevel(level);
        purview.setLevelExp(Purview.Expression.EQ);
        purview.setMode(mode);
        purview.setScheme(scheme);
        purview.setCondition(EMPTY);
        purview.setPretreatment(false);
        setPurview(purview);
    }

    public static void setPurview(PurviewContext purviewContext) {
        Assert.notNull(purviewContext.getType(), "purview type can not be null!");
        Assert.notNull(purviewContext.getKey(), "purview key can not be null!");
        CONTEXT.set(purviewContext);
    }

    @Override
    public void close() {
        CONTEXT.remove();
    }
}
