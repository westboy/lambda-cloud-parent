package com.lambda.cloud.core.annotation;

import java.lang.annotation.*;

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
}
