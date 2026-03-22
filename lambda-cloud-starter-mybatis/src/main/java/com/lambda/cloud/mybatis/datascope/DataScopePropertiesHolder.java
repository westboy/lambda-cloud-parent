package com.lambda.cloud.mybatis.datascope;

import com.lambda.autoconfig.datascope.DataScopeProperties;
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
public class DataScopePropertiesHolder {

    private static final AtomicReference<DataScopeProperties> REF = new AtomicReference<>(new DataScopeProperties());

    public static DataScopeProperties getInstance() {
        return REF.get();
    }

    public static void initialize(DataScopeProperties properties) {
        Assert.notNull(properties, "PurviewProperties must not be null");
        REF.set(properties);
    }
}
