package com.lambda.cloud.netty.protocol.accessor;

/**
 * 字段访问器接口
 * <p>
 * 用于替代反射的高性能字段访问接口
 * </p>
 *
 * @author Jin
 */
public interface FieldAccessor {

    /**
     * 设置字段值
     *
     * @param target 目标对象
     * @param value  字段值
     */
    void setValue(Object target, Object value);

    /**
     * 获取字段值
     *
     * @param target 目标对象
     * @return 字段值
     */
    Object getValue(Object target);

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    String getFieldName();

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    Class<?> getFieldType();

    void setFieldName(String fieldName);

    void setFieldType(Class<?> fieldType);
}
