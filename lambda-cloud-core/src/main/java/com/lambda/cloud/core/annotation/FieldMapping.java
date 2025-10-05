package com.lambda.cloud.core.annotation;

import java.lang.annotation.*;

/**
 * 字段映射注解
 * <p>
 * 用于定义源对象和目标对象之间的字段映射关系。
 * 该注解可以应用在类或字段上。
 * <p>
 * 当应用在类上时，定义该类的字段映射规则；
 * 当应用在字段上时，定义该字段的映射规则。
 * <p>
 * 该注解的配置会被转换为 MapStruct 的 @Mapping 注解。
 *
 * @author Jin
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@Documented
@Repeatable(FieldMappings.class)
public @interface FieldMapping {

    /**
     * 目标字段名
     * <p>
     * 指定映射到目标对象的字段名。
     */
    String target();

    /**
     * 源字段名
     * <p>
     * 指定从源对象映射的字段名。
     * 如果为空，则使用目标字段名作为源字段名。
     */
    String source() default "";

    /**
     * 是否忽略该字段
     * <p>
     * 如果设置为 true，则在映射过程中忽略该字段。
     */
    boolean ignore() default false;

    /**
     * 日期格式
     * <p>
     * 用于日期类型字段的格式化。
     */
    String dateFormat() default "";

    /**
     * 数字格式
     * <p>
     * 用于数字类型字段的格式化。
     */
    String numberFormat() default "";

    /**
     * 区域设置
     * <p>
     * 用于格式化的区域设置。
     */
    String locale() default "";

    /**
     * 表达式
     * <p>
     * 用于复杂映射的 Java 表达式。
     */
    String expression() default "";

    /**
     * 默认表达式
     * <p>
     * 当源字段为 null 时使用的默认表达式。
     */
    String defaultExpression() default "";

    /**
     * 默认值
     * <p>
     * 当源字段为 null 时使用的默认值。
     */
    String defaultValue() default "";

    /**
     * 限定名称
     * <p>
     * 用于指定映射方法的限定名称。
     */
    String qualifiedByName() default "";

    /**
     * 条件限定类
     * <p>
     * 用于条件映射的限定类。
     */
    Class<?>[] conditionQualifiedBy() default {};

    /**
     * 限定类
     * <p>
     * 用于映射的限定类。
     */
    Class<?>[] qualifiedBy() default {};

    /**
     * 条件表达式
     * <p>
     * 用于条件映射的表达式。
     */
    String conditionExpression() default "";

    /**
     * 条件限定名称
     * <p>
     * 用于条件映射的限定名称。
     */
    String conditionQualifiedByName() default "";
}
