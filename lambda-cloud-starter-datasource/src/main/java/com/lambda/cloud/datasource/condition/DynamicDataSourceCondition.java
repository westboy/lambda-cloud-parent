package com.lambda.cloud.datasource.condition;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * DynamicDataSourceCondition
 *
 * @author e
 */
public class DynamicDataSourceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
        String datasource = context.getEnvironment().getProperty("spring.datasource.dynamic.primary");
        return StringUtils.hasText(datasource);
    }
}
