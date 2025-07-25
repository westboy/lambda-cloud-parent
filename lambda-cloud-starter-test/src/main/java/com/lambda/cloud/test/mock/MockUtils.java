package com.lambda.cloud.test.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Mock 工具类。
 * <p>
 * 该工具类提供便捷的 Mock 对象创建、配置和管理功能，
 * 简化单元测试中的 Mock 操作，提高测试代码的可读性和维护性。
 * <p>
 * 主要功能：
 * <ul>
 *     <li>快速创建和配置 Mock 对象</li>
 *     <li>批量注入 Mock 依赖</li>
 *     <li>静态方法 Mock 管理</li>
 *     <li>Mock 对象重置和清理</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 创建并配置 Mock 对象
 * UserRepository mockRepo = MockUtils.createMock(UserRepository.class, mock -> {
 *     when(mock.findById(anyLong())).thenReturn(Optional.of(new User()));
 * });
 *
 * // 批量注入 Mock 依赖
 * UserService service = new UserService();
 * MockUtils.injectMocks(service)
 *     .inject("userRepository", mockRepo)
 *     .inject("emailService", mockEmailService);
 * </pre>
 *
 * @author Jin
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("all")
public class MockUtils {

    private static final Map<Class<?>, MockedStatic<?>> staticMocks = new HashMap<>();

    /**
     * 创建指定类型的 Mock 对象。
     *
     * @param clazz Mock 对象类型
     * @param <T>   泛型类型
     * @return Mock 对象实例
     */
    public static <T> T createMock(Class<T> clazz) {
        return Mockito.mock(clazz);
    }

    /**
     * 创建并配置 Mock 对象。
     *
     * @param clazz       Mock 对象类型
     * @param configurator Mock 配置器
     * @param <T>         泛型类型
     * @return 配置后的 Mock 对象实例
     */
    public static <T> T createMock(Class<T> clazz, Consumer<T> configurator) {
        T mock = Mockito.mock(clazz);
        configurator.accept(mock);
        return mock;
    }

    /**
     * 创建 Spy 对象。
     *
     * @param object 真实对象实例
     * @param <T>    泛型类型
     * @return Spy 对象实例
     */
    public static <T> T createSpy(T object) {
        return Mockito.spy(object);
    }

    /**
     * 创建并配置 Spy 对象。
     *
     * @param object       真实对象实例
     * @param configurator Spy 配置器
     * @param <T>          泛型类型
     * @return 配置后的 Spy 对象实例
     */
    public static <T> T createSpy(T object, Consumer<T> configurator) {
        T spy = Mockito.spy(object);
        configurator.accept(spy);
        return spy;
    }

    /**
     * 创建依赖注入器。
     *
     * @param target 目标对象
     * @param <T>    泛型类型
     * @return 依赖注入器实例
     */
    public static <T> DependencyInjector<T> injectMocks(T target) {
        return new DependencyInjector<>(target);
    }

    /**
     * Mock 静态方法。
     *
     * @param clazz 包含静态方法的类
     * @param <T>   泛型类型
     * @return MockedStatic 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> MockedStatic<T> mockStatic(Class<T> clazz) {
        MockedStatic<T> mockedStatic = Mockito.mockStatic(clazz);
        staticMocks.put(clazz, mockedStatic);
        return mockedStatic;
    }

    /**
     * Mock 静态方法并配置。
     *
     * @param clazz        包含静态方法的类
     * @param configurator 静态 Mock 配置器
     * @param <T>          泛型类型
     * @return MockedStatic 实例
     */
    public static <T> MockedStatic<T> mockStatic(Class<T> clazz, Consumer<MockedStatic<T>> configurator) {
        MockedStatic<T> mockedStatic = mockStatic(clazz);
        configurator.accept(mockedStatic);
        return mockedStatic;
    }

    /**
     * 重置指定的 Mock 对象。
     *
     * @param mocks Mock 对象数组
     */
    public static void resetMocks(Object... mocks) {
        for (Object mock : mocks) {
            if (mock != null) {
                Mockito.reset(mock);
            }
        }
    }

    /**
     * 清理所有静态 Mock。
     */
    public static void clearStaticMocks() {
        staticMocks.values().forEach(MockedStatic::close);
        staticMocks.clear();
    }

    /**
     * 验证 Mock 对象的交互。
     *
     * @param mock Mock 对象
     * @param <T>  泛型类型
     * @return Mock 对象（用于链式调用）
     */
    public static <T> T verify(T mock) {
        return Mockito.verify(mock);
    }

    /**
     * 验证 Mock 对象的交互次数。
     *
     * @param mock  Mock 对象
     * @param times 期望的调用次数
     * @param <T>   泛型类型
     * @return Mock 对象（用于链式调用）
     */
    public static <T> T verify(T mock, int times) {
        return Mockito.verify(mock, Mockito.times(times));
    }

    /**
     * 验证 Mock 对象从未被调用。
     *
     * @param mock Mock 对象
     * @param <T>  泛型类型
     * @return Mock 对象（用于链式调用）
     */
    public static <T> T verifyNever(T mock) {
        return Mockito.verify(mock, Mockito.never());
    }

    /**
     * 依赖注入器内部类。
     *
     * @param <T> 目标对象类型
     */
    public static class DependencyInjector<T> {
        private final T target;

        private DependencyInjector(T target) {
            this.target = target;
        }

        /**
         * 注入指定字段的依赖。
         *
         * @param fieldName 字段名
         * @param dependency 依赖对象
         * @return 当前注入器实例
         */
        public DependencyInjector<T> inject(String fieldName, Object dependency) {
            try {
                ReflectionTestUtils.setField(target, fieldName, dependency);
                log.debug("成功注入依赖: {}.{}", target.getClass().getSimpleName(), fieldName);
            } catch (Exception e) {
                log.error("注入依赖失败: {}.{}", target.getClass().getSimpleName(), fieldName, e);
                throw new RuntimeException("注入依赖失败", e);
            }
            return this;
        }

        /**
         * 批量注入依赖。
         *
         * @param dependencies 依赖映射（字段名 -> 依赖对象）
         * @return 当前注入器实例
         */
        public DependencyInjector<T> injectAll(Map<String, Object> dependencies) {
            dependencies.forEach(this::inject);
            return this;
        }

        /**
         * 获取目标对象。
         *
         * @return 目标对象实例
         */
        public T getTarget() {
            return target;
        }
    }
}
