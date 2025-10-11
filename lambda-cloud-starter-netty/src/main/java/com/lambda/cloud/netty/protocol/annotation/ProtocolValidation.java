package com.lambda.cloud.netty.protocol.annotation;

import java.lang.annotation.*;

/**
 * 协议字段验证注解
 * <p>
 * 用于对协议字段进行数据验证
 * </p>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolValidation {

    /**
     * 最小值（适用于数值类型）
     *
     * @return 最小值
     */
    long min() default Long.MIN_VALUE;

    /**
     * 最大值（适用于数值类型）
     *
     * @return 最大值
     */
    long max() default Long.MAX_VALUE;

    /**
     * 最小长度（适用于字符串类型）
     *
     * @return 最小长度
     */
    int minLength() default 0;

    /**
     * 最大长度（适用于字符串类型）
     *
     * @return 最大长度
     */
    int maxLength() default Integer.MAX_VALUE;

    /**
     * 正则表达式验证（适用于字符串类型）
     *
     * @return 正则表达式
     */
    String pattern() default "";

    /**
     * 是否允许为空
     *
     * @return true表示允许为空
     */
    boolean nullable() default true;

    /**
     * 自定义验证器类
     *
     * @return 验证器类
     */
    Class<? extends ProtocolValidator> validator() default ProtocolValidator.class;

    /**
     * 验证失败时的错误消息
     *
     * @return 错误消息
     */
    String message() default "字段验证失败";
}
