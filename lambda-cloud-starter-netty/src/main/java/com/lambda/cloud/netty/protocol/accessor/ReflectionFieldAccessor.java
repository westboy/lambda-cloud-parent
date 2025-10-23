package com.lambda.cloud.netty.protocol.accessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class ReflectionFieldAccessor implements FieldAccessor {

    private final MethodHandle setter;
    private final MethodHandle getter;
    private final Field field;

    public ReflectionFieldAccessor(Field field) {
        try {
            this.field = field;
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
            throw new RuntimeException("Failed to set field " + field.getName(), e);
        }
    }

    public Object getValue(Object target) {
        try {
            return getter.invoke(target);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get field " + field.getName(), e);
        }
    }

    @Override
    public String getFieldName() {
        return field.getName();
    }

    @Override
    public Class<?> getFieldType() {
        return field.getType();
    }
}
