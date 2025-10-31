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
@Inherited
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
     * 加密标识
     * 在encryptedKey字段值为 0x01时 encryptedField 字段生效
     *
     * @return 加密标识
     */
    boolean encryptedKey() default false;

    /**
     * 是否加密字段
     *
     * @return 是否加密
     */
    boolean encryptedField() default false;

    /**
     * computed 计算字段
     *
     * <p>
     * 表示该字段为校验字段，参与CRC校验值与长度计算的计算
     * </p>
     *
     * @return 校验
     */
    boolean computed() default false;

    /**
     * CRCFiled
     * <p>
     * 表示该字段为CRC校验字段，存储校验值，需要根据其他字段计算CRC校验值
     * ---
     * 解析的时候需要取该值参与CRC校验
     * 序列化的时候需要将计算出的CRC校验值写入该字段
     * </p>
     *
     * @return CRC字段
     */
    boolean crcFiled() default false;

    /**
     * 长度字段
     * <p>
     * 表示该字段为长度字段，存储数据长度，需要根据其他字段计算数据长度
     * ---
     * 解析的时候需要取该值参与数据长度计算
     * 序列化的时候需要将计算出的数据长度写入该字段
     * </p>
     * @return
     */
    boolean lengthFiled() default false;

    /**
     * 序号字段
     * <p>
     * 表示该字段为序号字段，存储消息序号，需要根据其他字段计算序号值
     * ---
     * 解析的时候需要取该值参与序号计算
     * 序列化的时候需要将计算出的序号值写入该字段
     * </p>
     * @return
     */
    boolean serialFiled() default false;

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

    /**
     * List元素类型（仅对LIST数据类型有效）
     * <p>
     * 指定List中元素的数据类型，用于正确解析和序列化List元素
     * </p>
     *
     * @return List元素的数据类型
     */
    ProtocolDataType listElementType() default ProtocolDataType.HEX;

    /**
     * List元素长度
     * <p>
     * 指定List中每个元素的字节长度，用于正确解析变长元素
     * 当值为0时，将根据元素类型自动确定长度
     * </p>
     *
     * @return 元素字节长度，0表示自动确定
     */
    int listLength() default 0;

    /**
     * List固定长度（仅对LIST数据类型有效）
     * <p>
     * 指定List中元素的固定数量，当值大于0时，List将包含固定数量的元素
     * </p>
     *
     * @return List元素数量
     */
    int listElementSize() default 0;
}
