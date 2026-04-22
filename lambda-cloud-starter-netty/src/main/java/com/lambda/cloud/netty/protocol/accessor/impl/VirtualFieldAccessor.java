package com.lambda.cloud.netty.protocol.accessor.impl;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VirtualFieldAccessor implements FieldAccessor {

    private String name;
    private Class<?> type;

    public VirtualFieldAccessor(String fieldName, Class<?> fieldType) {
        this.name = fieldName;
        this.type = fieldType;
    }

    @Override
    public void setValue(Object target, Object value) {
        throw new UnsupportedOperationException("VirtualFieldAccessor does not support setValue");
    }

    @Override
    public Object getValue(Object target) {
        throw new UnsupportedOperationException("VirtualFieldAccessor does not support getValue");
    }

    @Override
    public String getFieldName() {
        return name;
    }

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
}
