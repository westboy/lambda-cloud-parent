package com.lambda.cloud.netty.protocol.accessor;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 字段访问器性能测试
 * <p>
 * 比较反射、MethodHandle 和字节码三种字段访问方式的性能差异
 * </p>
 *
 * @author Jin
 */
public class FieldAccessorPerformanceTest {

    /**
     * 测试用的数据类
     */
    public static class TestData {
        public int intValue;
        public long longValue;
        public String stringValue;
        public boolean booleanValue;
        public double doubleValue;

        public TestData() {}

        public TestData(int intValue, long longValue, String stringValue, boolean booleanValue, double doubleValue) {
            this.intValue = intValue;
            this.longValue = longValue;
            this.stringValue = stringValue;
            this.booleanValue = booleanValue;
            this.doubleValue = doubleValue;
        }

        // Getters and Setters
        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public long getLongValue() {
            return longValue;
        }

        public void setLongValue(long longValue) {
            this.longValue = longValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(double doubleValue) {
            this.doubleValue = doubleValue;
        }
    }

    private static final int WARMUP_ITERATIONS = 10000;
    private static final int TEST_ITERATIONS = 1000000;

    private TestData testObject;
    private Field intField;
    private Field longField;
    private Field stringField;
    private Field booleanField;
    private Field doubleField;

    private FieldAccessor reflectionIntAccessor;
    private FieldAccessor bytecodeIntAccessor;
    private FieldAccessor reflectionStringAccessor;
    private FieldAccessor bytecodeStringAccessor;

    @BeforeEach
    public void setUp() throws Exception {
        testObject = new TestData(42, 123456789L, "test", true, 3.14);

        // 获取字段
        intField = TestData.class.getDeclaredField("intValue");
        longField = TestData.class.getDeclaredField("longValue");
        stringField = TestData.class.getDeclaredField("stringValue");
        booleanField = TestData.class.getDeclaredField("booleanValue");
        doubleField = TestData.class.getDeclaredField("doubleValue");

        // 创建访问器
        reflectionIntAccessor =
                FieldAccessorFactory.createAccessor(intField, FieldAccessorFactory.AccessorType.REFLECTION);
        bytecodeIntAccessor = FieldAccessorFactory.createAccessor(intField, FieldAccessorFactory.AccessorType.BYTECODE);
        reflectionStringAccessor =
                FieldAccessorFactory.createAccessor(stringField, FieldAccessorFactory.AccessorType.REFLECTION);
        bytecodeStringAccessor =
                FieldAccessorFactory.createAccessor(stringField, FieldAccessorFactory.AccessorType.BYTECODE);
    }

    @Test
    public void testIntFieldPerformance() throws Exception {
        System.out.println("=== Int Field Performance Test ===");

        // 预热
        warmup();

        // 测试直接访问
        long directTime = testDirectIntAccess();
        System.out.printf("Direct access: %d ms%n", directTime);

        // 测试传统反射
        long reflectionTime = testTraditionalReflection();
        System.out.printf("Traditional reflection: %d ms%n", reflectionTime);

        // 测试 MethodHandle 反射访问器
        long methodHandleTime = testReflectionAccessor();
        System.out.printf("MethodHandle accessor: %d ms%n", methodHandleTime);

        // 测试字节码访问器
        long bytecodeTime = testBytecodeAccessor();
        System.out.printf("Bytecode accessor: %d ms%n", bytecodeTime);

        // 性能比较
        System.out.println("\n=== Performance Comparison ===");
        System.out.printf("MethodHandle vs Direct: %.2fx slower%n", (double) methodHandleTime / directTime);
        System.out.printf("Bytecode vs Direct: %.2fx slower%n", (double) bytecodeTime / directTime);
        System.out.printf("Bytecode vs MethodHandle: %.2fx faster%n", (double) methodHandleTime / bytecodeTime);
        System.out.printf("Bytecode vs Traditional Reflection: %.2fx faster%n", (double) reflectionTime / bytecodeTime);
    }

    @Test
    public void testStringFieldPerformance() throws Exception {
        System.out.println("\n=== String Field Performance Test ===");

        // 预热
        warmupString();

        // 测试 MethodHandle 访问器
        long methodHandleTime = testReflectionStringAccessor();
        System.out.printf("MethodHandle string accessor: %d ms%n", methodHandleTime);

        // 测试字节码访问器
        long bytecodeTime = testBytecodeStringAccessor();
        System.out.printf("Bytecode string accessor: %d ms%n", bytecodeTime);

        // 性能比较
        System.out.println("\n=== String Field Performance Comparison ===");
        System.out.printf("Bytecode vs MethodHandle: %.2fx faster%n", (double) methodHandleTime / bytecodeTime);
    }

    @Test
    public void testAllFieldTypes() throws Exception {
        System.out.println("\n=== All Field Types Performance Test ===");

        Field[] fields = {intField, longField, stringField, booleanField, doubleField};
        String[] fieldNames = {"int", "long", "String", "boolean", "double"};

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String fieldName = fieldNames[i];

            FieldAccessor reflectionAccessor =
                    FieldAccessorFactory.createAccessor(field, FieldAccessorFactory.AccessorType.REFLECTION);
            FieldAccessor bytecodeAccessor =
                    FieldAccessorFactory.createAccessor(field, FieldAccessorFactory.AccessorType.BYTECODE);

            // 预热
            for (int j = 0; j < WARMUP_ITERATIONS; j++) {
                reflectionAccessor.getValue(testObject);
                bytecodeAccessor.getValue(testObject);
            }

            // 测试 MethodHandle
            long startTime = System.nanoTime();
            for (int j = 0; j < TEST_ITERATIONS; j++) {
                reflectionAccessor.getValue(testObject);
            }
            long methodHandleTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            // 测试字节码
            startTime = System.nanoTime();
            for (int j = 0; j < TEST_ITERATIONS; j++) {
                bytecodeAccessor.getValue(testObject);
            }
            long bytecodeTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            System.out.printf(
                    "%s field - MethodHandle: %d ms, Bytecode: %d ms, Speedup: %.2fx%n",
                    fieldName, methodHandleTime, bytecodeTime, (double) methodHandleTime / bytecodeTime);
        }
    }

    @Test
    public void testCachePerformance() {
        System.out.println("\n=== Cache Performance Test ===");

        // 测试缓存启用时的性能
        FieldAccessorFactory.setCacheEnabled(true);
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            FieldAccessorFactory.createAccessor(intField, FieldAccessorFactory.AccessorType.BYTECODE);
        }
        long cachedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // 清空缓存并测试无缓存时的性能
        FieldAccessorFactory.setCacheEnabled(false);
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            FieldAccessorFactory.createAccessor(intField, FieldAccessorFactory.AccessorType.BYTECODE);
        }
        long uncachedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        System.out.printf("Cached creation: %d ms%n", cachedTime);
        System.out.printf("Uncached creation: %d ms%n", uncachedTime);
        System.out.printf("Cache speedup: %.2fx%n", (double) uncachedTime / cachedTime);

        // 恢复缓存设置
        FieldAccessorFactory.setCacheEnabled(true);
    }

    private void warmup() throws Exception {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            // 直接访问
            testObject.getIntValue();
            testObject.setIntValue(i);

            // 传统反射
            intField.setAccessible(true);
            intField.get(testObject);
            intField.set(testObject, i);

            // MethodHandle 访问器
            reflectionIntAccessor.getValue(testObject);
            reflectionIntAccessor.setValue(testObject, i);

            // 字节码访问器
            bytecodeIntAccessor.getValue(testObject);
            bytecodeIntAccessor.setValue(testObject, i);
        }
    }

    private void warmupString() {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            reflectionStringAccessor.getValue(testObject);
            bytecodeStringAccessor.getValue(testObject);
        }
    }

    private long testDirectIntAccess() {
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            testObject.getIntValue();
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private long testTraditionalReflection() throws Exception {
        intField.setAccessible(true);
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            intField.get(testObject);
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private long testReflectionAccessor() {
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            reflectionIntAccessor.getValue(testObject);
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private long testBytecodeAccessor() {
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            bytecodeIntAccessor.getValue(testObject);
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private long testReflectionStringAccessor() {
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            reflectionStringAccessor.getValue(testObject);
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private long testBytecodeStringAccessor() {
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            bytecodeStringAccessor.getValue(testObject);
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }
}
