package com.lambda.cloud.netty.protocol.accessor.impl;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import com.lambda.cloud.netty.protocol.accessor.asm.FieldAccessorGenerator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import lombok.Getter;
import lombok.Setter;

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
@Setter
@SuppressFBWarnings("EI_EXPOSE_REP")
public class ByteCodeFieldAccessor implements FieldAccessor {

    private final FieldAccessor dynamicAccessor;
    private String name;
    private Class<?> type;

    /**
     * 构造函数
     *
     * @param field 目标字段
     * @throws RuntimeException 如果字节码生成失败
     */
    public ByteCodeFieldAccessor(Field field) {
        this.dynamicAccessor = FieldAccessorGenerator.generateAccessor(field);
        this.name = field.getName();
        this.type = field.getType();
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
        dynamicAccessor.setValue(target, value);
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
        return dynamicAccessor.getValue(target);
    }

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    @Override
    public String getFieldName() {
        return name;
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    @Override
    public Class<?> getFieldType() {
        return type;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.name = fieldName;
    }

    @Override
    public void setFieldType(Class<?> fieldType) {
        this.type = fieldType;
    }

    /**
     * 检查是否为基本类型字段
     *
     * @return 如果是基本类型返回 true
     */
    public boolean isPrimitive() {
        return type.isPrimitive();
    }

    /**
     * 获取访问器类型标识
     *
     * @return 访问器类型字符串
     */
    public String getAccessorType() {
        return "ByteCode";
    }
}
