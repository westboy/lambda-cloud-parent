package com.lamuda.cloud.mybatis.purview;


import cn.hutool.core.lang.Assert;
import com.lamuda.cloud.mybatis.purview.annotation.Purview;
import com.lamuda.cloud.mybatis.purview.support.DynamicPurview;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

/**
 * 动态数据权限管理器
 *
 * @author Jin
 */
public final class PurviewHelper implements AutoCloseable {

    private static final ThreadLocal<DynamicPurview> CONTEXT = new ThreadLocal<>();

    private static final PurviewHelper HELPER = new PurviewHelper();


    private PurviewHelper() {
    }


    public static PurviewHelper getInstance() {
        return HELPER;
    }


    public DynamicPurview getPurview() {
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
        DynamicPurview purview = new DynamicPurview();
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

    public static void setPurview(DynamicPurview dynamicPurview) {
        Assert.notNull(dynamicPurview.getType(), "purview type can not be null!");
        Assert.notNull(dynamicPurview.getKey(), "purview key can not be null!");
        CONTEXT.set(dynamicPurview);
    }


    @Override
    public void close() {
        CONTEXT.remove();
    }


}
