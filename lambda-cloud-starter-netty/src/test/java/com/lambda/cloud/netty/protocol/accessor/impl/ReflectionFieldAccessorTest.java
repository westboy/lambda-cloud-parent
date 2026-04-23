package com.lambda.cloud.netty.protocol.accessor.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ReflectionFieldAccessorTest {

    private String testField;

    @Test
    void testInitialization() throws NoSuchFieldException {
        Field field = ReflectionFieldAccessorTest.class.getDeclaredField("testField");
        ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(field);

        assertNotNull(accessor.getFieldName(), "Field name should not be null");
        assertEquals("testField", accessor.getFieldName(), "Field name should match");

        assertNotNull(accessor.getFieldType(), "Field type should not be null");
        assertEquals(String.class, accessor.getFieldType(), "Field type should match");
    }
}
