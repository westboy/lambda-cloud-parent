package com.lambda.cloud.test.assertion;

import cn.hutool.core.thread.ThreadUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

/**
 * Lambda Cloud 断言工具类。
 * <p>
 * 该工具类扩展了标准的断言功能，提供更丰富的断言方法和自定义断言，
 * 特别针对 Lambda Cloud 项目的业务场景进行了优化。
 * <p>
 * 主要功能：
 * <ul>
 *     <li>扩展的集合断言</li>
 *     <li>时间相关断言</li>
 *     <li>异步操作断言</li>
 *     <li>业务对象断言</li>
 *     <li>性能断言</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 基础断言
 * LambdaAssertions.assertThat(user).isNotNull();
 *
 * // 集合断言
 * LambdaAssertions.assertThat(users).hasSize(5).allMatch(u -> u.getAge() > 18);
 *
 * // 时间断言
 * LambdaAssertions.assertThat(user.getCreateTime()).isRecentWithin(Duration.ofMinutes(5));
 *
 * // 异步断言
 * LambdaAssertions.assertEventually(() -> service.isReady(), Duration.ofSeconds(10));
 * </pre>
 *
 * @author Jin
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("all")
public class LambdaAssertions extends Assertions {

    /**
     * 创建Lambda对象断言。
     *
     * @param actual 实际值
     * @param <T>    泛型类型
     * @return Lambda对象断言实例
     */
    public static <T> LambdaObjectAssert<T> assertLambda(T actual) {
        return new LambdaObjectAssert<>(actual);
    }

    /**
     * 创建Lambda集合断言。
     *
     * @param actual 实际集合
     * @param <T>    集合元素类型
     * @return Lambda集合断言实例
     */
    public static <T> LambdaCollectionAssert<T> assertLambda(Collection<T> actual) {
        return new LambdaCollectionAssert<>(actual);
    }

    /**
     * 创建Lambda时间断言。
     *
     * @param actual 实际时间
     * @return Lambda时间断言实例
     */
    public static LambdaDateTimeAssert assertLambda(LocalDateTime actual) {
        return new LambdaDateTimeAssert(actual);
    }

    /**
     * 断言最终条件成立（异步断言）。
     *
     * @param condition 条件供应商
     * @param timeout   超时时间
     */
    public static void assertEventually(Supplier<Boolean> condition, Duration timeout) {
        assertEventually(condition, timeout, Duration.ofMillis(100));
    }

    /**
     * 断言最终条件成立（异步断言）。
     *
     * @param condition 条件供应商
     * @param timeout   超时时间
     * @param interval  检查间隔
     */
    public static void assertEventually(Supplier<Boolean> condition, Duration timeout, Duration interval) {
        long timeoutMillis = timeout.toMillis();
        long intervalMillis = interval.toMillis();
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (condition.get()) {
                return;
            }
            ThreadUtil.safeSleep(intervalMillis);
        }

        throw new AssertionError(String.format("条件在 %d ms 内未满足", timeoutMillis));
    }

    /**
     * 断言操作在指定时间内完成。
     *
     * @param operation 操作
     * @param timeout   超时时间
     */
    public static void assertCompletesWithin(Runnable operation, Duration timeout) {
        long startTime = System.currentTimeMillis();
        operation.run();
        long duration = System.currentTimeMillis() - startTime;

        if (duration > timeout.toMillis()) {
            throw new AssertionError(String.format("操作耗时 %d ms，超过预期的 %d ms", duration, timeout.toMillis()));
        }
    }

    /**
     * 断言操作抛出指定类型的异常。
     *
     * @param exceptionType 期望的异常类型
     * @param operation     操作
     * @param <T>           异常类型
     * @return 抛出的异常实例
     */
    public static <T extends Throwable> T assertThrows(Class<T> exceptionType, Callable<?> operation) {
        try {
            operation.call();
            throw new AssertionError("期望抛出异常 " + exceptionType.getSimpleName() + "，但没有异常被抛出");
        } catch (Throwable throwable) {
            if (!exceptionType.isInstance(throwable)) {
                throw new AssertionError(
                        String.format(
                                "期望抛出异常 %s，但实际抛出 %s",
                                exceptionType.getSimpleName(),
                                throwable.getClass().getSimpleName()),
                        throwable);
            }
            return exceptionType.cast(throwable);
        }
    }

    /**
     * 断言操作抛出指定类型的异常（Runnable版本）。
     *
     * @param exceptionType 期望的异常类型
     * @param operation     操作
     * @param <T>           异常类型
     * @return 抛出的异常实例
     */
    public static <T extends Throwable> T assertThrows(Class<T> exceptionType, Runnable operation) {
        try {
            operation.run();
            throw new AssertionError("期望抛出异常 " + exceptionType.getSimpleName() + "，但没有异常被抛出");
        } catch (Throwable throwable) {
            if (!exceptionType.isInstance(throwable)) {
                throw new AssertionError(
                        String.format(
                                "期望抛出异常 %s，但实际抛出 %s",
                                exceptionType.getSimpleName(),
                                throwable.getClass().getSimpleName()),
                        throwable);
            }
            return exceptionType.cast(throwable);
        }
    }

    /**
     * 断言操作不抛出任何异常。
     *
     * @param operation 操作
     */
    public static void assertDoesNotThrow(Runnable operation) {
        try {
            operation.run();
        } catch (Throwable throwable) {
            throw new AssertionError("期望操作不抛出异常，但抛出了: " + throwable.getClass().getSimpleName(), throwable);
        }
    }

    /**
     * 断言操作不抛出任何异常（Callable版本）。
     *
     * @param operation 操作
     * @param <T>       返回类型
     * @return 操作的返回值
     */
    public static <T> T assertDoesNotThrow(Callable<T> operation) {
        try {
            return operation.call();
        } catch (Throwable throwable) {
            throw new AssertionError("期望操作不抛出异常，但抛出了: " + throwable.getClass().getSimpleName(), throwable);
        }
    }

    /**
     * 断言性能：操作执行时间应在指定范围内。
     *
     * @param operation   操作
     * @param minDuration 最小执行时间
     * @param maxDuration 最大执行时间
     */
    public static void assertExecutionTime(Runnable operation, Duration minDuration, Duration maxDuration) {
        if (minDuration == null || maxDuration == null) {
            throw new AssertionError("时间范围参数不能为 null");
        }
        if (minDuration.compareTo(maxDuration) > 0) {
            throw new AssertionError("最小执行时间不能大于最大执行时间");
        }

        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        long actualDuration = endTime - startTime;

        long minNanos = minDuration.toNanos();
        long maxNanos = maxDuration.toNanos();

        if (actualDuration < minNanos) {
            throw new AssertionError(String.format("操作执行时间 %d ns 小于最小期望时间 %d ns", actualDuration, minNanos));
        }
        if (actualDuration > maxNanos) {
            throw new AssertionError(String.format("操作执行时间 %d ns 超过最大期望时间 %d ns", actualDuration, maxNanos));
        }
    }

    /**
     * 扩展的对象断言类。
     *
     * @param <T> 对象类型
     */
    public static class LambdaObjectAssert<T> extends ObjectAssert<T> {

        public LambdaObjectAssert(T actual) {
            super(actual);
        }

        /**
         * 断言对象不为null。
         *
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> isNotNull() {
            if (actual == null) {
                throw new AssertionError("对象为 null");
            }
            return this;
        }

        /**
         * 断言对象为null。
         */
        public void isNull() {
            if (actual != null) {
                throw new AssertionError(String.format("期望对象为 null，但实际为: %s", actual));
            }
        }

        /**
         * 断言对象等于指定值。
         *
         * @param expected 期望值
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> isEqualTo(Object expected) {
            if (!java.util.Objects.equals(actual, expected)) {
                throw new AssertionError(String.format("对象 %s 不等于期望值 %s", actual, expected));
            }
            return this;
        }

        /**
         * 断言对象不等于指定值。
         *
         * @param other 比较值
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> isNotEqualTo(Object other) {
            if (java.util.Objects.equals(actual, other)) {
                throw new AssertionError(String.format("对象 %s 不应等于 %s", actual, other));
            }
            return this;
        }

        /**
         * 断言对象是指定类型的实例。
         *
         * @param expectedType 期望类型
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> isInstanceOf(Class<?> expectedType) {
            if (actual == null) {
                throw new AssertionError("对象为 null，无法检查类型");
            }
            if (expectedType == null) {
                throw new AssertionError("期望类型不能为 null");
            }
            if (!expectedType.isInstance(actual)) {
                throw new AssertionError(String.format(
                        "对象 %s 不是 %s 的实例，实际类型为 %s",
                        actual, expectedType.getName(), actual.getClass().getName()));
            }
            return this;
        }

        /**
         * 断言对象满足指定条件。
         *
         * @param predicate 条件谓词
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> matches(Predicate<? super T> predicate) {
            if (actual == null) {
                throw new AssertionError("对象为 null，无法进行条件匹配");
            }
            if (predicate == null) {
                throw new AssertionError("条件谓词不能为 null");
            }
            if (!predicate.test(actual)) {
                throw new AssertionError("对象不满足指定条件");
            }
            return this;
        }

        /**
         * 断言对象具有指定的属性值。
         * 支持多种getter方法命名规范：getXxx、isXxx（布尔类型）、直接属性名。
         *
         * @param propertyName  属性名，不能为null或空字符串
         * @param expectedValue 期望值，可以为null
         * @return 当前断言实例
         * @throws AssertionError 当对象为null、属性名无效、找不到getter方法或属性值不匹配时
         */
        public LambdaObjectAssert<T> hasProperty(String propertyName, Object expectedValue) {
            if (actual == null) {
                throw new AssertionError("对象为 null，无法检查属性");
            }
            validatePropertyName(propertyName);
            try {
                Method getter = findGetterMethod(actual.getClass(), propertyName);
                Object actualValue = getter.invoke(actual);

                if (!Objects.equals(actualValue, expectedValue)) {
                    throw new AssertionError(String.format(
                            "属性 '%s' 的实际值为 [%s]，期望值为 [%s]",
                            propertyName,
                            actualValue != null ? actualValue : "null",
                            expectedValue != null ? expectedValue : "null"));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new AssertionError(
                        String.format("调用属性 '%s' 的getter方法时发生错误: %s", propertyName, e.getMessage()), e);
            }
            return this;
        }

        /**
         * 验证属性名的有效性。
         *
         * @param propertyName 属性名
         * @throws AssertionError 当属性名无效时
         */
        private void validatePropertyName(String propertyName) {
            if (propertyName == null) {
                throw new AssertionError("属性名不能为 null");
            }
            if (propertyName.trim().isEmpty()) {
                throw new AssertionError("属性名不能为空字符串");
            }
            if (propertyName.length() == 1) {
                // 单字符属性名需要特殊处理
                if (!Character.isJavaIdentifierPart(propertyName.charAt(0))) {
                    throw new AssertionError("属性名必须是一个有效的Java标识符");
                }
            }
        }

        /**
         * 查找指定属性的getter方法。
         * 按以下顺序尝试：
         * 1. getPropertyName
         * 2. isPropertyName（适用于布尔类型）
         * 3. propertyName（直接方法名）
         *
         * @param clazz        目标类
         * @param propertyName 属性名
         * @return getter方法
         * @throws AssertionError 当找不到合适的getter方法时
         */
        private Method findGetterMethod(Class<?> clazz, String propertyName) {
            String capitalizedProperty = propertyName.length() == 1
                    ? propertyName.toUpperCase()
                    : propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

            // 尝试标准getter方法
            String getterName = "get" + capitalizedProperty;
            try {
                return clazz.getMethod(getterName);
            } catch (NoSuchMethodException ignored) {
                // 继续尝试其他方法
            }

            // 尝试布尔类型的is方法
            String isGetterName = "is" + capitalizedProperty;
            try {
                Method isMethod = clazz.getMethod(isGetterName);
                // 验证返回类型是否为布尔类型
                Class<?> returnType = isMethod.getReturnType();
                if (returnType == boolean.class || returnType == Boolean.class) {
                    return isMethod;
                }
            } catch (NoSuchMethodException ignored) {
                // 继续尝试其他方法
            }

            // 尝试直接使用属性名作为方法名
            try {
                return clazz.getMethod(propertyName);
            } catch (NoSuchMethodException ignored) {
                // 所有尝试都失败
            }

            // 所有方法都找不到，抛出详细的错误信息
            throw new AssertionError(String.format(
                    "在类 %s 中未找到属性 '%s' 的getter方法。尝试了以下方法名：%s, %s, %s",
                    clazz.getSimpleName(), propertyName, getterName, isGetterName, propertyName));
        }

        /**
         * 断言对象的toString()包含指定文本。
         *
         * @param text 期望包含的文本
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> toStringContains(String text) {
            if (actual == null) {
                throw new AssertionError("对象为 null，无法检查字符串表示");
            }
            if (text == null) {
                throw new AssertionError("期望文本不能为 null");
            }
            String actualString = actual.toString();
            if (!actualString.contains(text)) {
                throw new AssertionError(String.format("对象的字符串表示 '%s' 不包含期望文本 '%s'", actualString, text));
            }
            return this;
        }

        /**
         * 断言对象的hashCode等于指定值。
         *
         * @param expectedHashCode 期望的hashCode
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> hasHashCode(int expectedHashCode) {
            if (actual == null) {
                throw new AssertionError("对象为 null，无法检查hashCode");
            }
            int actualHashCode = actual.hashCode();
            if (actualHashCode != expectedHashCode) {
                throw new AssertionError(String.format("对象的hashCode为 %d，期望为 %d", actualHashCode, expectedHashCode));
            }
            return this;
        }

        /**
         * 断言对象与另一个对象相同（使用==比较）。
         *
         * @param expected 期望的对象
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> isSameAs(Object expected) {
            if (actual != expected) {
                throw new AssertionError(String.format("对象 %s 与期望对象 %s 不是同一个实例", actual, expected));
            }
            return this;
        }

        /**
         * 断言对象与另一个对象不相同（使用==比较）。
         *
         * @param other 比较的对象
         * @return 当前断言实例
         */
        public LambdaObjectAssert<T> isNotSameAs(Object other) {
            if (actual == other) {
                throw new AssertionError(String.format("对象 %s 与 %s 是同一个实例，但期望不同", actual, other));
            }
            return this;
        }
    }
}
