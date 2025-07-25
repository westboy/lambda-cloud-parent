package com.lambda.cloud.test.assertion;

import static com.lambda.cloud.test.assertion.LambdaAssertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * LambdaAssertions 工具类的测试示例。
 * <p>
 * 该测试类展示了如何使用 LambdaAssertions 提供的各种断言方法，
 * 包括对象断言、集合断言、时间断言、异常断言和性能断言等。
 *
 * @author Jin
 * @since 1.0.0
 */
@SuppressWarnings("all")
class LambdaAssertionsTest {

    @Test
    void testObjectAssertions() {
        // 对象断言示例
        String testString = "Hello World";

        // 使用assertLambda方法进行对象断言
        assertLambda(testString)
                .isNotNull()
                .isEqualTo("Hello World")
                .isNotEqualTo("Goodbye")
                .isInstanceOf(String.class)
                .toStringContains("World")
                .hasHashCode(testString.hashCode())
                .isSameAs(testString)
                .isNotSameAs("Hello World!"); // 不同的字符串实例

        // 也可以使用assertThat方法（扩展版本）
        assertThat(testString).isNotNull();
    }

    @Test
    void testPropertyAssertions() {
        // 创建测试对象
        TestObject testObj = new TestObject("test", 42, true);

        // 测试hasProperty方法的各种情况
        assertLambda(testObj)
                .hasProperty("name", "test") // 标准getter
                .hasProperty("value", 42) // 数值属性
                .hasProperty("active", true); // 布尔属性（is方法）

        // 测试null值属性
        TestObject nullObj = new TestObject(null, 0, false);
        assertLambda(nullObj).hasProperty("name", null).hasProperty("value", 0).hasProperty("active", false);
    }

    @Test
    void testPropertyAssertionsErrorCases() {
        TestObject testObj = new TestObject("test", 42, true);

        // 测试属性名为null的情况
        assertThrows(AssertionError.class, () -> {
            assertLambda(testObj).hasProperty(null, "test");
        });

        // 测试属性名为空字符串的情况
        assertThrows(AssertionError.class, () -> {
            assertLambda(testObj).hasProperty("", "test");
        });

        // 测试属性名为空白字符串的情况
        assertThrows(AssertionError.class, () -> {
            assertLambda(testObj).hasProperty("   ", "test");
        });

        // 测试不存在的属性
        assertThrows(AssertionError.class, () -> {
            assertLambda(testObj).hasProperty("nonExistent", "test");
        });

        // 测试属性值不匹配的情况
        assertThrows(AssertionError.class, () -> {
            assertLambda(testObj).hasProperty("name", "wrong");
        });

        // 测试对象为null的情况
        assertThrows(AssertionError.class, () -> {
            assertLambda((TestObject) null).hasProperty("name", "test");
        });
    }

    // 测试用的内部类
    private record TestObject(String name, int value, boolean active) {}

    @Test
    void testCollectionAssertions() {
        // 集合断言示例
        List<String> list = Arrays.asList("apple", "banana", "cherry");

        // 使用assertLambda方法进行集合断言
        LambdaCollectionAssert<String> stringLambdaCollectionAssert = assertLambda(list)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .contains("apple")
                .doesNotContain("orange")
                .allMatch(s -> s.length() > 3)
                .anyMatch(s -> s.startsWith("a"))
                .noneMatch(s -> s.startsWith("z"));

        LambdaCollectionAssert<Object> objectLambdaCollectionAssert = assertLambda(Collections.emptyList());

        // 也可以使用assertThat方法（扩展版本）
        assertThat(list).hasSize(3);
    }

    @Test
    void testDateTimeAssertions() {
        // 时间断言示例
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusHours(1);
        LocalDateTime future = now.plusHours(1);

        // 使用assertLambda方法进行时间断言
        assertLambda(now)
                .isNotNull()
                .isAfter(past)
                .isBefore(future)
                .isAfterOrEqualTo(past)
                .isBeforeOrEqualTo(future)
                .isBetween(past, future)
                .isToday()
                .isCloseTo(now, 1); // 1秒容忍度

        // 也可以使用assertThat方法（扩展版本）
        assertThat(now).isNotNull();
    }

    @Test
    void testExceptionAssertions() {
        // 异常断言示例
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("测试异常");
        });

        assertThat(exception.getMessage()).contains("测试异常");

        // 测试不抛出异常
        assertDoesNotThrow(() -> {
            // 正常操作
            String result = "success";
        });

        String result = assertDoesNotThrow(() -> "返回值");
        assertThat(result).isEqualTo("返回值");
    }

    @Test
    void testAsyncAssertions() {
        // 异步断言示例
        assertEventually(
                () -> {
                    // 模拟异步操作最终成功
                    return true;
                },
                Duration.ofSeconds(5));

        // 性能断言示例
        assertCompletesWithin(
                () -> {
                    // 快速操作
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                },
                Duration.ofMillis(100));

        // 执行时间范围断言
        assertExecutionTime(
                () -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                },
                Duration.ofMillis(40),
                Duration.ofMillis(100));
    }

    /**
     * 测试用的简单 Person 类。
     */
    private record Person(String name, int age) {

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }
}
