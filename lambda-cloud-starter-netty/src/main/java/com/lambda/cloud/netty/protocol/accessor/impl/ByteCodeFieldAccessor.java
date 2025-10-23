package com.lambda.cloud.netty.protocol.accessor.impl;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import com.lambda.cloud.netty.protocol.accessor.asm.FieldAccessorGenerator;
import lombok.Getter;

import java.lang.reflect.Field;

/**
 * 基于字节码生成的高性能字段访问器
 * <p>
 * 使用ASM字节码生成技术创建动态访问器，提供比反射更高的性能。
 * 内部委托给 {@link FieldAccessorGenerator} 生成的动态类实例。
 * </p>
 *
 * @author Jin
 * @see FieldAccessorGenerator
 * @see ReflectionFieldAccessor
 */
@Getter
public class ByteCodeFieldAccessor implements FieldAccessor {

    /**
     * 动态生成的字段访问器实例
     *
     */
    private final FieldAccessor dynamicAccessor;

    /**
     * 目标字段信息
     *
     */
    private final Field field;

    /**
     * 构造函数
     *
     * @param field 目标字段
     * @throws RuntimeException 如果字节码生成失败
     */
    public ByteCodeFieldAccessor(Field field) {
        this.field = field;
        try {
            this.dynamicAccessor = FieldAccessorGenerator.generateAccessor(field);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate bytecode accessor for field: "
                            + field.getDeclaringClass().getName() + "#" + field.getName(),
                    e);
        }
    }

    /**
     * 设置字段值
     * <p>
     * 委托给动态生成的访问器执行，避免反射调用的性能开销
     * </p>
     *
     * @param target 目标对象
     * @param value  字段值
     * @throws RuntimeException 如果设置失败
     */
    @Override
    public void setValue(Object target, Object value) {
        try {
            dynamicAccessor.setValue(target, value);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to set field value: " + field.getDeclaringClass().getName() + "#" + field.getName(), e);
        }
    }

    /**
     * 获取字段值
     * <p>
     * 委托给动态生成的访问器执行，避免反射调用的性能开销
     * </p>
     *
     * @param target 目标对象
     * @return 字段值
     * @throws RuntimeException 如果获取失败
     */
    @Override
    public Object getValue(Object target) {
        try {
            return dynamicAccessor.getValue(target);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to get field value: " + field.getDeclaringClass().getName() + "#" + field.getName(), e);
        }
    }

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    @Override
    public String getFieldName() {
        return field.getName();
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    @Override
    public Class<?> getFieldType() {
        return field.getType();
    }

    /**
     * 检查是否为基本类型字段
     *
     * @return 如果是基本类型返回 true
     */
    public boolean isPrimitive() {
        return field.getType().isPrimitive();
    }

    /**
     * 获取访问器类型标识
     *
     * @return 访问器类型字符串
     */
    public String getAccessorType() {
        return "ByteCode";
    }

    @Override
    public String toString() {
        return "ByteCodeFieldAccessor{" + "field="
                + field.getDeclaringClass().getSimpleName() + "#" + field.getName() + ", type="
                + field.getType().getSimpleName() + ", dynamicAccessor="
                + dynamicAccessor.getClass().getSimpleName() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ByteCodeFieldAccessor that = (ByteCodeFieldAccessor) obj;
        return field.equals(that.field);
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }
}
