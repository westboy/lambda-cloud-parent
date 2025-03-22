package com.lamuda.cloud.datasource.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

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