package com.lambda.cloud.plugin.annotation;

import java.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * 插件注解
 *
 * @author Jin
 */
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Plugin {}
