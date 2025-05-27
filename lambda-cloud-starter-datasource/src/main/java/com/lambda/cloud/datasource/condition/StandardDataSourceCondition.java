package com.lambda.cloud.datasource.condition;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * StandDataSourceCondition
 *
 * @author w
 */
public class StandardDataSourceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        String datasource1 = context.getEnvironment().getProperty("spring.datasource.url");
        String datasource2 = context.getEnvironment().getProperty("spring.datasource.dynamic.primary");
        return StringUtils.isNotBlank(datasource1) && StringUtils.isBlank(datasource2);
    }
}
