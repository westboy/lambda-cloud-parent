package com.lambda.cloud.netty.protocol.annotation;

import java.lang.annotation.*;

/**
 * 协议消息注解
 * <p>
 * 用于标记协议消息类，提供消息级别的配置
 * </p>
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ProtocolFrame {

    /**
     * 消息类型标识
     *
     * @return 消息类型
     */
    String frameType() default "";

    /**
     * 消息名称
     *
     * @return 消息名称
     */
    String name() default "";

    /**
     * 消息描述
     *
     * @return 消息描述
     */
    String description() default "";

    /**
     * 启用加密的值
     *
     * @return 0 不启用 1 启用
     */
    int encryptFlag() default 0;

    /**
     * 消息版本
     *
     * @return 消息版本
     */
    String version() default "1.0";

    /**
     * 是否启用严格模式（严格按照字段顺序解析）
     *
     * @return true 表示启用严格模式
     */
    boolean strictMode() default true;

    /**
     * 默认字符编码（可被字段级别的设置覆盖）
     *
     * @return 默认字符编码
     */
    String defaultCharset() default "UTF-8";
}
