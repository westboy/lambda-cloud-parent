package com.lamuda.cloud.mybatis.support;


import org.apache.commons.lang.BooleanUtils;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * @author Jin
 */
public class TypeConverter {
    private static final String ERROR = "Cannot cast %s [%s] to %s.";

    private TypeConverter() {
    }

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

    private static boolean toBoolean(Object value) {
        if (ClassUtils.isAssignableValue(Integer.class, value)) {
            return BooleanUtils.toBoolean(toIntValue(value));
        } else {
            return BooleanUtils.toBoolean(String.valueOf(value));
        }
    }

    private static Date toDate(Object value) {
        if (ClassUtils.isAssignableValue(Instant.class, value)) {
            return new Date(((Instant) value).toEpochMilli());
        }
        String message = String.format(ERROR, value.getClass().getName(), value, Date.class);
        throw new ClassCastException(message);
    }

    private static double toDoubleValue(final Object value) {
        if (double.class.isAssignableFrom(value.getClass()) || Double.class.isAssignableFrom(value.getClass())) {
            return (double) value;
        }
        return 0.0;
    }

    private static long toLongValue(final Object value) {
        if (long.class.isAssignableFrom(value.getClass()) || Long.class.isAssignableFrom(value.getClass())) {
            return (long) value;
        }
        return ((Double) value).longValue();
    }

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