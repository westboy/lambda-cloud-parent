package com.lambda.autoconfig.condition;

import cn.hutool.core.util.StrUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MapperPackageConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        String property = context.getEnvironment().getProperty("mybatis-plus.mapper-package");
        return StrUtil.isNotEmpty(property);
    }
}
