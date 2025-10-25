package com.lambda.cloud.netty.protocol.accessor.impl;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReflectionFieldAccessor implements FieldAccessor {

    private final MethodHandle setter;
    private final MethodHandle getter;
    private String name;
    private Class<?> type;

    public ReflectionFieldAccessor(Field field) {
        try {
            field.setAccessible(true);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            this.setter = lookup.unreflectSetter(field);
            this.getter = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to create MethodHandle for " + field, e);
        }
    }

    public void setValue(Object target, Object value) {
        try {
            setter.invoke(target, value);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to set field " + name, e);
        }
    }

    public Object getValue(Object target) {
        try {
            return getter.invoke(target);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get field " + name, e);
        }
    }

    @Override
    public void setFieldName(String fieldName) {
        this.name = fieldName;
    }

    @Override
    public void setFieldType(Class<?> fieldType) {
        this.type = fieldType;
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public Class<?> getFieldType() {
        return type;
    }
}
