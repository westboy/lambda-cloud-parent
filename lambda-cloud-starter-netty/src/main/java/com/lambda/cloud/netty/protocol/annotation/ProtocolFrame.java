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
     * crc 算法
     * @return   crc 算法 名称
     */
    String crcAlgorithm() default "CRC16-MODBUS";

    /**
     * 消息描述
     *
     * @return 消息描述
     */
    String description() default "";

    /**
     * 消息版本
     *
     * @return 消息版本
     */
    String version() default "1.0";

    /**
     * 是否为消息主体
     *
     * @return true 消息主体
     */
    boolean isPayload() default false;

    /**
     * 默认字符编码（可被字段级别的设置覆盖）
     *
     * @return 默认字符编码
     */
    String defaultCharset() default "UTF-8";
}
