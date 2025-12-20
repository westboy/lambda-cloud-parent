package com.lambda.cloud.mybatis.purview.config;

import com.lambda.autoconfig.PurviewProperties;
import com.lambda.cloud.core.utils.Assert;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.UtilityClass;

/**
 * 数据权限配置持有者
 * 用于在非 Spring 管理的类中访问配置
 *
 * @author Jin
 */
@UtilityClass
public class PurviewPropertiesHolder {

    private static final AtomicReference<PurviewProperties> REF = new AtomicReference<>(new PurviewProperties());

    public static PurviewProperties getInstance() {
        return REF.get();
    }

    public static void initialize(PurviewProperties properties) {
        Assert.notNull(properties, "PurviewProperties must not be null");
        REF.set(properties);
    }
}
