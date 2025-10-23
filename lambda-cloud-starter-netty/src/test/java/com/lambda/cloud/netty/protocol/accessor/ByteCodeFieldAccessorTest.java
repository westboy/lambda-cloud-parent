package com.lambda.cloud.netty.protocol.accessor;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.accessor.impl.ByteCodeFieldAccessor;
import com.lambda.cloud.netty.protocol.accessor.impl.ReflectionFieldAccessor;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ByteCodeFieldAccessor 单元测试
 *
 * @author Jin
 */
public class ByteCodeFieldAccessorTest {

    /**
     * 测试用的数据类
     */
    public static class TestData {
        public int intValue;
        public long longValue;
        public String stringValue;
        public boolean booleanValue;
        public double doubleValue;
        public Object objectValue;

        public TestData() {}

        public TestData(int intValue, long longValue, String stringValue, boolean booleanValue, double doubleValue) {
            this.intValue = intValue;
            this.longValue = longValue;
            this.stringValue = stringValue;
            this.booleanValue = booleanValue;
            this.doubleValue = doubleValue;
        }
    }

    private TestData testObject;
    private Field intField;
    private Field longField;
    private Field stringField;
    private Field booleanField;
    private Field doubleField;
    private Field objectField;

    @BeforeEach
    public void setUp() throws Exception {
        testObject = new TestData(42, 123456789L, "test", true, 3.14);

        intField = TestData.class.getDeclaredField("intValue");
        longField = TestData.class.getDeclaredField("longValue");
        stringField = TestData.class.getDeclaredField("stringValue");
        booleanField = TestData.class.getDeclaredField("booleanValue");
        doubleField = TestData.class.getDeclaredField("doubleValue");
        objectField = TestData.class.getDeclaredField("objectValue");
    }

    @Test
    public void testIntFieldAccess() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(intField);

        // 测试获取值
        Object value = accessor.getValue(testObject);
        assertEquals(42, value);
        assertTrue(value instanceof Integer);

        // 测试设置值
        accessor.setValue(testObject, 100);
        assertEquals(100, accessor.getValue(testObject));
    }

    @Test
    public void testLongFieldAccess() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(longField);

        // 测试获取值
        Object value = accessor.getValue(testObject);
        assertEquals(123456789L, value);
        assertTrue(value instanceof Long);

        // 测试设置值
        accessor.setValue(testObject, 999999999L);
        assertEquals(999999999L, accessor.getValue(testObject));
    }

    @Test
    public void testStringFieldAccess() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(stringField);

        // 测试获取值
        Object value = accessor.getValue(testObject);
        assertEquals("test", value);
        assertTrue(value instanceof String);

        // 测试设置值
        accessor.setValue(testObject, "new value");
        assertEquals("new value", accessor.getValue(testObject));

        // 测试设置 null
        accessor.setValue(testObject, null);
        assertNull(accessor.getValue(testObject));
    }

    @Test
    public void testBooleanFieldAccess() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(booleanField);

        // 测试获取值
        Object value = accessor.getValue(testObject);
        assertEquals(true, value);
        assertTrue(value instanceof Boolean);

        // 测试设置值
        accessor.setValue(testObject, false);
        assertEquals(false, accessor.getValue(testObject));
    }

    @Test
    public void testDoubleFieldAccess() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(doubleField);

        // 测试获取值
        Object value = accessor.getValue(testObject);
        assertEquals(3.14, value);
        assertTrue(value instanceof Double);

        // 测试设置值
        accessor.setValue(testObject, 2.71);
        assertEquals(2.71, accessor.getValue(testObject));
    }

    @Test
    public void testObjectFieldAccess() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(objectField);

        // 测试获取值（初始为 null）
        assertNull(accessor.getValue(testObject));

        // 测试设置值
        Object testObj = new Object();
        accessor.setValue(testObject, testObj);
        assertSame(testObj, accessor.getValue(testObject));

        // 测试设置 null
        accessor.setValue(testObject, null);
        assertNull(accessor.getValue(testObject));
    }

    @Test
    public void testFieldMetadata() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(intField);

        assertEquals("intValue", accessor.getFieldName());
        assertEquals(int.class, accessor.getFieldType());
        assertSame(intField, accessor.getField());
        assertTrue(accessor.isPrimitive());
        assertEquals("ByteCode", accessor.getAccessorType());
    }

    @Test
    public void testStringFieldMetadata() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(stringField);

        assertEquals("stringValue", accessor.getFieldName());
        assertEquals(String.class, accessor.getFieldType());
        assertSame(stringField, accessor.getField());
        assertFalse(accessor.isPrimitive());
        assertEquals("ByteCode", accessor.getAccessorType());
    }

    @Test
    public void testDynamicAccessorNotNull() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(intField);
        assertNotNull(accessor.getDynamicAccessor());
        assertTrue(accessor.getDynamicAccessor() instanceof FieldAccessor);
    }

    @Test
    public void testToString() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(intField);
        String toString = accessor.toString();

        assertTrue(toString.contains("ByteCodeFieldAccessor"));
        assertTrue(toString.contains("TestData#intValue"));
        assertTrue(toString.contains("int"));
    }

    @Test
    public void testEquals() {
        ByteCodeFieldAccessor accessor1 = new ByteCodeFieldAccessor(intField);
        ByteCodeFieldAccessor accessor2 = new ByteCodeFieldAccessor(intField);
        ByteCodeFieldAccessor accessor3 = new ByteCodeFieldAccessor(stringField);

        assertEquals(accessor1, accessor2);
        assertNotEquals(accessor1, accessor3);
        assertNotEquals(accessor1, null);
        assertNotEquals(accessor1, "not an accessor");
    }

    @Test
    public void testHashCode() {
        ByteCodeFieldAccessor accessor1 = new ByteCodeFieldAccessor(intField);
        ByteCodeFieldAccessor accessor2 = new ByteCodeFieldAccessor(intField);

        assertEquals(accessor1.hashCode(), accessor2.hashCode());
    }

    @Test
    public void testNullFieldThrowsException() {
        assertThrows(RuntimeException.class, () -> {
            new ByteCodeFieldAccessor(null);
        });
    }

    @Test
    public void testInvalidTargetObjectThrowsException() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(intField);

        // 使用错误类型的目标对象
        assertThrows(RuntimeException.class, () -> {
            accessor.getValue("wrong type");
        });

        assertThrows(RuntimeException.class, () -> {
            accessor.setValue("wrong type", 42);
        });
    }

    @Test
    public void testInvalidValueTypeThrowsException() {
        ByteCodeFieldAccessor accessor = new ByteCodeFieldAccessor(intField);

        // 尝试设置错误类型的值
        assertThrows(RuntimeException.class, () -> {
            accessor.setValue(testObject, "not an int");
        });
    }

    @Test
    public void testCompareWithReflectionAccessor() {
        ByteCodeFieldAccessor bytecodeAccessor = new ByteCodeFieldAccessor(intField);
        ReflectionFieldAccessor reflectionAccessor = new ReflectionFieldAccessor(intField);

        // 测试获取值的一致性
        assertEquals(reflectionAccessor.getValue(testObject), bytecodeAccessor.getValue(testObject));

        // 测试设置值的一致性
        bytecodeAccessor.setValue(testObject, 200);
        assertEquals(200, reflectionAccessor.getValue(testObject));

        reflectionAccessor.setValue(testObject, 300);
        assertEquals(300, bytecodeAccessor.getValue(testObject));
    }
}
