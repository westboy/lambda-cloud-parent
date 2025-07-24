package com.lambda.cloud.core.convert;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.util.ClassUtils;

/**
 * 类型转换工具类
 * <p>
 * 提供各种基本数据类型之间的安全转换功能。该工具类支持常见的类型转换操作，
 * 包括数值类型、字符串、布尔值、日期等类型之间的相互转换。
 * </p>
 *
 * <h3>支持的转换类型：</h3>
 * <ul>
 *   <li>String - 字符串转换</li>
 *   <li>Long/Integer - 数值类型转换</li>
 *   <li>Double - 浮点数转换</li>
 *   <li>Boolean - 布尔值转换</li>
 *   <li>BigDecimal - 高精度数值转换</li>
 *   <li>Date - 日期类型转换</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 数值转换
 * Integer intValue = (Integer) TypeConverter.convert("123", Integer.class);
 * Long longValue = (Long) TypeConverter.convert(123.45, Long.class);
 *
 * // 字符串转换
 * String strValue = (String) TypeConverter.convert(123, String.class);
 *
 * // 布尔值转换
 * Boolean boolValue = (Boolean) TypeConverter.convert(1, Boolean.class);
 *
 * // BigDecimal转换
 * BigDecimal decimal = (BigDecimal) TypeConverter.convert("123.45", BigDecimal.class);
 * }</pre>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>所有方法都进行空值检查，不接受null参数</li>
 *   <li>不支持的转换会抛出ClassCastException</li>
 *   <li>转换失败时会提供详细的错误信息</li>
 *   <li>该类为工具类，构造函数为私有</li>
 * </ul>
 *
 * @author Jin
 * @see java.lang.ClassCastException
 * @see org.springframework.util.ClassUtils
 */
public class TypeConverter {
    private static final String ERROR = "Cannot cast %s [%s] to %s.";

    private TypeConverter() {}

    /**
     * 通用类型转换方法
     * <p>
     * 将给定的对象转换为指定的目标类型。该方法会根据目标类型自动选择
     * 合适的转换策略，支持常见的基本数据类型转换。
     * </p>
     *
     * <h3>转换规则：</h3>
     * <ul>
     *   <li>如果源对象已经是目标类型，直接返回</li>
     *   <li>目标类型为String时，调用相应的toString方法</li>
     *   <li>目标类型为数值类型时，进行数值转换</li>
     *   <li>目标类型为Boolean时，进行布尔值转换</li>
     *   <li>目标类型为Date时，从Instant转换</li>
     * </ul>
     *
     * @param value 源对象，不能为null
     * @param expect 目标类型，不能为null
     * @return 转换后的对象
     * @throws NullPointerException 如果参数为null
     * @throws ClassCastException 如果不支持该类型转换
     */
    public static Object convert(@Nonnull final Object value, @Nonnull final Class<?> expect) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(expect);
        if (ClassUtils.isAssignableValue(expect, value)) {
            return value;
        } else if (ClassUtils.isAssignable(String.class, expect)) {
            if (ClassUtils.isAssignableValue(Long.class, value)) {
                return Long.toString(toLongValue(value));
            } else if (ClassUtils.isAssignableValue(Integer.class, value)) {
                return Integer.toString(toIntValue(value));
            } else {
                return String.valueOf(value);
            }
        } else if (ClassUtils.isAssignable(Long.class, expect)) {
            return toLongValue(value);
        } else if (ClassUtils.isAssignable(Integer.class, expect)) {
            return toIntValue(value);
        } else if (ClassUtils.isAssignable(Double.class, expect)) {
            return toDoubleValue(value);
        } else if (ClassUtils.isAssignable(Boolean.class, expect)) {
            return toBoolean(value);
        } else if (ClassUtils.isAssignable(BigDecimal.class, expect)) {
            return toBigDecimalValue(value);
        } else if (ClassUtils.isAssignable(Date.class, expect)) {
            return toDate(value);
        }
        return value;
    }

    /**
     * 转换为布尔值
     * <p>
     * 将对象转换为布尔值。支持从Integer和String类型转换。
     * </p>
     *
     * @param value 源对象
     * @return 转换后的布尔值
     */
    private static boolean toBoolean(Object value) {
        if (ClassUtils.isAssignableValue(Integer.class, value)) {
            return BooleanUtils.toBoolean(toIntValue(value));
        } else {
            return BooleanUtils.toBoolean(String.valueOf(value));
        }
    }

    /**
     * 转换为日期类型
     * <p>
     * 将对象转换为Date类型。目前仅支持从Instant类型转换。
     * </p>
     *
     * @param value 源对象
     * @return 转换后的Date对象
     * @throws ClassCastException 如果不支持该类型转换
     */
    private static Date toDate(Object value) {
        if (ClassUtils.isAssignableValue(Instant.class, value)) {
            return new Date(((Instant) value).toEpochMilli());
        }
        String message = String.format(ERROR, value.getClass().getName(), value, Date.class);
        throw new ClassCastException(message);
    }

    /**
     * 转换为双精度浮点数
     * <p>
     * 将对象转换为double类型。支持从double和Double类型转换。
     * </p>
     *
     * @param value 源对象
     * @return 转换后的double值，不支持的类型返回0.0
     */
    private static double toDoubleValue(final Object value) {
        if (double.class.isAssignableFrom(value.getClass()) || Double.class.isAssignableFrom(value.getClass())) {
            return (double) value;
        }
        return 0.0;
    }

    /**
     * 转换为长整型
     * <p>
     * 将对象转换为long类型。支持从long、Long和Double类型转换。
     * </p>
     *
     * @param value 源对象
     * @return 转换后的long值
     */
    private static long toLongValue(final Object value) {
        if (long.class.isAssignableFrom(value.getClass()) || Long.class.isAssignableFrom(value.getClass())) {
            return (long) value;
        }
        return ((Double) value).longValue();
    }

    /**
     * 转换为整型
     * <p>
     * 将对象转换为int类型。支持从int、Integer、double、Double、
     * BigDecimal和Long类型转换。
     * </p>
     *
     * @param value 源对象
     * @return 转换后的int值
     */
    private static int toIntValue(final Object value) {
        if (int.class.isAssignableFrom(value.getClass()) || Integer.class.isAssignableFrom(value.getClass())) {
            return (int) value;
        }
        if (double.class.isAssignableFrom(value.getClass()) || Double.class.isAssignableFrom(value.getClass())) {
            return ((Double) value).intValue();
        }
        if (BigDecimal.class.isAssignableFrom(value.getClass())) {
            return ((BigDecimal) value).intValue();
        }
        return ((Long) value).intValue();
    }

    /**
     * 转换为高精度数值
     * <p>
     * 将对象转换为BigDecimal类型。支持从String、double、Double、
     * int、Integer、long和Long类型转换。
     * </p>
     *
     * @param value 源对象
     * @return 转换后的BigDecimal对象
     * @throws ClassCastException 如果不支持该类型转换
     */
    private static BigDecimal toBigDecimalValue(final Object value) {
        if (String.class.isAssignableFrom(value.getClass())) {
            return new BigDecimal((String) value);
        }

        if (double.class.isAssignableFrom(value.getClass()) || Double.class.isAssignableFrom(value.getClass())) {
            return BigDecimal.valueOf((double) value);
        }

        if (int.class.isAssignableFrom(value.getClass()) || Integer.class.isAssignableFrom(value.getClass())) {
            return BigDecimal.valueOf((int) value);
        }

        if (long.class.isAssignableFrom(value.getClass()) || Long.class.isAssignableFrom(value.getClass())) {
            return BigDecimal.valueOf((long) value);
        }

        String message = String.format(ERROR, value.getClass().getName(), value, BigDecimal.class);

        throw new ClassCastException(message);
    }
}
