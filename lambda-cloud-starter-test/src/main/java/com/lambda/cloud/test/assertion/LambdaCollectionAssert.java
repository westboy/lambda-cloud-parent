package com.lambda.cloud.test.assertion;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * 扩展的集合断言类。
 *
 * @param <T> 集合元素类型
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public record LambdaCollectionAssert<T>(Collection<T> actual) {

    /**
     * 断言集合不为null。
     *
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> isNotNull() {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        return this;
    }

    /**
     * 断言集合为空。
     *
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> isEmpty() {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        if (!actual.isEmpty()) {
            throw new AssertionError(String.format("期望集合为空，但实际大小为 %d", actual.size()));
        }
        return this;
    }

    /**
     * 断言集合不为空。
     *
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> isNotEmpty() {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        if (actual.isEmpty()) {
            throw new AssertionError("期望集合不为空，但实际为空");
        }
        return this;
    }

    /**
     * 断言集合大小。
     *
     * @param expectedSize 期望大小
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> hasSize(int expectedSize) {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        if (expectedSize < 0) {
            throw new AssertionError("期望大小不能为负数");
        }
        if (actual.size() != expectedSize) {
            throw new AssertionError(String.format("集合大小为 %d，期望为 %d", actual.size(), expectedSize));
        }
        return this;
    }

    /**
     * 断言集合包含指定元素。
     *
     * @param element 期望包含的元素
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> contains(T element) {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        if (!actual.contains(element)) {
            throw new AssertionError(String.format("集合不包含元素: %s", element));
        }
        return this;
    }

    /**
     * 断言集合不包含指定元素。
     *
     * @param element 不应包含的元素
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> doesNotContain(T element) {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        if (actual.contains(element)) {
            throw new AssertionError(String.format("集合不应包含元素: %s", element));
        }
        return this;
    }

    /**
     * 断言集合所有元素都满足条件。
     *
     * @param predicate 条件谓词
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> allMatch(Predicate<T> predicate) {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        if (predicate == null) {
            throw new AssertionError("条件谓词不能为 null");
        }
        if (!actual.stream().allMatch(predicate)) {
            throw new AssertionError("集合中存在不满足条件的元素");
        }
        return this;
    }

    /**
     * 断言集合至少有一个元素满足条件。
     *
     * @param predicate 条件谓词
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> anyMatch(Predicate<T> predicate) {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        if (predicate == null) {
            throw new AssertionError("条件谓词不能为 null");
        }
        if (!actual.stream().anyMatch(predicate)) {
            throw new AssertionError("集合中没有满足条件的元素");
        }
        return this;
    }

    /**
     * 断言集合没有元素满足条件。
     *
     * @param predicate 条件谓词
     * @return 当前断言实例
     */
    public LambdaCollectionAssert<T> noneMatch(Predicate<T> predicate) {
        if (actual == null) {
            throw new AssertionError("集合为 null");
        }
        if (predicate == null) {
            throw new AssertionError("条件谓词不能为 null");
        }
        if (actual.stream().anyMatch(predicate)) {
            throw new AssertionError("集合中存在满足条件的元素，但期望没有");
        }
        return this;
    }
}
