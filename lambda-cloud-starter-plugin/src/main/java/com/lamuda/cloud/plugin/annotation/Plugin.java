package com.lambda.cloud.plugin.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 插件注解
 *
 * @author Jin
 */
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Plugin {
}
