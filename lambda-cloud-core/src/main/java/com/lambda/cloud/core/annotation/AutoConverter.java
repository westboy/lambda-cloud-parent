package com.lambda.cloud.core.annotation;

import java.lang.annotation.*;

/**
 * 自动生成转换接口。
 * <p>
 * 默认情况下，生成的接口继承 BaseConverter<S, T>，并实现其抽象方法。
 * <p>
 * 如果指定了 converter 属性，则生成的接口继承该属性指定的接口，并实现其抽象方法。
 * <p>
 * 如果指定了 uses 属性，则生成的接口会添加 @Mapper.uses 配置。
 * <p>
 * 如果指定了 config 属性，则生成的接口会添加 @Mapper.config 配置。
 * <p>
 * 如果指定了 fieldMappings 属性，则生成的接口方法会添加相应的 @Mapping 注解。
 * <p>
 * 也可以直接在类上使用 @FieldMapping 注解来定义字段映射。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface AutoConverter {

    /**
     * 父转换接口。
     * <p>
     * 如果指定，则生成的 Converter 接口会继承该接口；
     * 如果未指定，则默认继承 BaseConverter<S, T>。
     */
    Class<?> converter() default Void.class;

    /**
     * 目标对象类型。
     * <p>
     * 用于指定 DTO、VO 转换到的目标类。
     */
    Class<?> target();

    /**
     * MapStruct 的辅助类（参考 @Mapper.uses 配置）。
     */
    Class<?>[] uses() default {};

    /**
     * MapStruct 的公共配置类（参考  @Mapper.config 配置）。
     */
    Class<?> config() default void.class;

    /**
     * 字段映射配置
     * <p>
     * 定义源对象和目标对象之间的字段映射关系。
     * 这些配置会被转换为 MapStruct 的 @Mapping 注解。
     * <p>
     * 注意：也可以直接在类上使用 @FieldMapping 注解来定义字段映射。
     */
    FieldMapping[] fieldMappings() default {};
}
