package com.lambda.cloud.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface AutoMapper {
    Class<?> target();
}
