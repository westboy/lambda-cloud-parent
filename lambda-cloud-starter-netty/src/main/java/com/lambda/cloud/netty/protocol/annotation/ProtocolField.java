package com.lambda.cloud.netty.protocol.annotation;

import java.lang.annotation.*;

/**
 * 协议字段注解
 * <p>
 * 用于标记协议消息中的字段，支持自动解析和映射
 * </p>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolField {

    /**
     * 字段在协议中的顺序（从0开始）
     *
     * @return 字段顺序
     */
    int order();

    /**
     * 是否符合字段
     * <p/>
     * 则表示该字段为一个类， 需要收集该类字段并从中取 @ProtocolField 注解 ，还有种情况就是 该字段的为加密字段 需要解密后才能解析
     *
     * @return 是否符合
     */
    boolean composite() default false;

    /**
     * 是否加密字段
     *
     * @return 是否加密
     */
    boolean encrypted() default false;

    /**
     * CRCChecksum 校验
     *
     * @return 校验
     */
    boolean CRCChecksum() default false;

    /**
     * 字段长度（字节数）
     *
     * @return 字段长度
     */
    int length() default 0;

    /**
     * 数据类型
     *
     * @return 数据类型
     */
    ProtocolDataType dataType() default ProtocolDataType.HEX;

    /**
     * /**
     * 获取字段的精度（小数位数）
     *
     * <p>用于描述数值类型字段的小数精度</p>
     *
     * @return 小数位数
     */
    int precision() default 0;

    /**
     * 是否为小端字节序
     *
     * @return true表示小端，false表示大端
     */
    boolean littleEndian() default false;

    /**
     * 字段描述
     *
     * @return 字段描述
     */
    String description() default "";

    /**
     * 是否为可选字段
     *
     * @return true表示可选，false表示必填
     */
    boolean optional() default false;

    /**
     * 默认值（当字段为可选且数据不足时使用）
     *
     * @return 默认值
     */
    String defaultValue() default "";

    /**
     * 字符编码（仅对ASCII类型有效）
     *
     * @return 字符编码
     */
    String charset() default "UTF-8";

    /**
     * 填充方向（仅对需要填充的类型有效）
     *
     * @return 填充方向
     */
    PaddingDirection padding() default PaddingDirection.LEFT;

    /**
     * 填充字符（仅对需要填充的类型有效）
     *
     * @return 填充字符
     */
    String paddingChar() default "0";
}
