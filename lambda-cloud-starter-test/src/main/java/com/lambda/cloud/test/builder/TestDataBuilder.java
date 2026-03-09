package com.lambda.cloud.test.builder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * 测试数据构建器工具类。
 * <p>
 * 该工具类提供便捷的测试数据生成功能，支持自动填充对象属性、
 * 生成随机数据、构建复杂对象等，简化测试数据准备工作。
 * <p>
 * 主要功能：
 * <ul>
 *     <li>自动生成基础类型的随机数据</li>
 *     <li>支持自定义属性值设置</li>
 *     <li>提供常用测试数据模板</li>
 *     <li>支持对象深度复制和转换</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 创建用户测试数据
 * User user = TestDataBuilder.create(User.class)
 *     .with("name", "张三")
 *     .with("age", 25)
 *     .build();
 *
 * // 批量创建测试数据
 * List&lt;User&gt; users = TestDataBuilder.createList(User.class, 10);
 * </pre>
 *
 * @author Jin
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("all")
@SuppressFBWarnings("REC_CATCH_EXCEPTION")
public class TestDataBuilder<T> {

    private final Class<T> targetClass;
    private final Map<String, Object> customValues = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TestDataBuilder(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * 创建指定类型的测试数据构建器。
     *
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 测试数据构建器实例
     */
    public static <T> TestDataBuilder<T> create(Class<T> clazz) {
        return new TestDataBuilder<>(clazz);
    }

    /**
     * 设置指定属性的值。
     *
     * @param fieldName 属性名
     * @param value     属性值
     * @return 当前构建器实例
     */
    public TestDataBuilder<T> with(String fieldName, Object value) {
        customValues.put(fieldName, value);
        return this;
    }

    /**
     * 构建测试数据对象。
     *
     * @return 填充了测试数据的对象实例
     */
    public T build() {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();
            fillFields(instance);
            return instance;
        } catch (Exception e) {
            log.error("创建测试数据失败: {}", targetClass.getSimpleName(), e);
            throw new RuntimeException("创建测试数据失败", e);
        }
    }

    /**
     * 批量创建指定数量的测试数据。
     *
     * @param clazz 目标类型
     * @param count 数量
     * @param <T>   泛型类型
     * @return 测试数据列表
     */
    public static <T> List<T> createList(Class<T> clazz, int count) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(create(clazz).build());
        }
        return result;
    }

    /**
     * 深度复制对象。
     *
     * @param source 源对象
     * @param <T>    泛型类型
     * @return 复制后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T source) {
        try {
            String json = objectMapper.writeValueAsString(source);
            return (T) objectMapper.readValue(json, source.getClass());
        } catch (Exception e) {
            log.error("深度复制对象失败", e);
            throw new RuntimeException("深度复制对象失败", e);
        }
    }

    /**
     * 填充对象字段。
     *
     * @param instance 目标对象实例
     */
    private void fillFields(T instance) {
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();

                Object value;
                if (customValues.containsKey(fieldName)) {
                    value = customValues.get(fieldName);
                } else {
                    value = generateRandomValue(field.getType());
                }

                if (value != null) {
                    field.set(instance, value);
                }
            } catch (Exception e) {
                log.debug("设置字段值失败: {}.{}", targetClass.getSimpleName(), field.getName());
            }
        }
    }

    /**
     * 生成指定类型的随机值。
     *
     * @param fieldType 字段类型
     * @return 随机值
     */
    private Object generateRandomValue(Class<?> fieldType) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (fieldType == String.class) {
            return "test_" + UUID.randomUUID().toString().substring(0, 8);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return random.nextInt(1, 1000);
        } else if (fieldType == Long.class || fieldType == long.class) {
            return random.nextLong(1L, 10000L);
        } else if (fieldType == Double.class || fieldType == double.class) {
            return random.nextDouble(1.0, 1000.0);
        } else if (fieldType == Float.class || fieldType == float.class) {
            return random.nextFloat() * 1000;
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return random.nextBoolean();
        } else if (fieldType == BigDecimal.class) {
            return BigDecimal.valueOf(random.nextDouble(1.0, 10000.0));
        } else if (fieldType == LocalDateTime.class) {
            return LocalDateTime.now().minusDays(random.nextInt(0, 365));
        } else if (fieldType == LocalDate.class) {
            return LocalDate.now().minusDays(random.nextInt(0, 365));
        } else if (fieldType == Date.class) {
            return new Date(System.currentTimeMillis() - random.nextLong(0, 365L * 24 * 60 * 60 * 1000));
        } else if (fieldType.isEnum()) {
            Object[] enumConstants = fieldType.getEnumConstants();
            return enumConstants[random.nextInt(enumConstants.length)];
        }

        return null;
    }
}
