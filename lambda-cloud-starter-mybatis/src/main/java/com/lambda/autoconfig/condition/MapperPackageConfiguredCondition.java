package com.lambda.autoconfig.condition;

import cn.hutool.core.util.StrUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MapperPackageConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        String kebab = context.getEnvironment().getProperty("mybatis-plus.mapper-package");
        String camel = context.getEnvironment().getProperty("mybatis-plus.mapperPackage");
        return StrUtil.isNotEmpty(kebab) || StrUtil.isNotEmpty(camel);
    }
}
