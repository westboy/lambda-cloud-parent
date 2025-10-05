package com.lambda.cloud.core.annotation;

import java.lang.annotation.*;

/**
 * 字段映射注解容器
 * <p>
 * 用于在同一个类或字段上定义多个 @FieldMapping 注解。
 * 这是 @FieldMapping 注解的容器注解，支持 @Repeatable 功能。
 * <p>
 * 通常不需要直接使用此注解，而是通过多次使用 @FieldMapping 注解，
 * Java 编译器会自动将其包装在 @FieldMappings 中。
 *
 * @author Jin
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface FieldMappings {

    /**
     * 字段映射注解数组
     * <p>
     * 包含多个 @FieldMapping 注解。
     */
    FieldMapping[] value();
}
