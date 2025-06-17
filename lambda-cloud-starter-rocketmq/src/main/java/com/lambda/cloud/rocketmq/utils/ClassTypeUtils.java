package com.lambda.cloud.rocketmq.utils;

public class ClassTypeUtils {
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class.equals(clazz)
                || Character.class.equals(clazz);
    }

    public static Object convertPrimitiveOrWrapper(Class<?> targetClass, String content) {
        if (Integer.class.equals(targetClass)) {
            return Integer.valueOf(content);
        } else if (Long.class.equals(targetClass)) {
            return Long.valueOf(content);
        } else if (Double.class.equals(targetClass)) {
            return Double.valueOf(content);
        } else if (Float.class.equals(targetClass)) {
            return Float.valueOf(content);
        } else if (Short.class.equals(targetClass)) {
            return Short.valueOf(content);
        } else if (Byte.class.equals(targetClass)) {
            return Byte.valueOf(content);
        } else if (Boolean.class.equals(targetClass)) {
            return Boolean.valueOf(content);
        } else if (Character.class.equals(targetClass)) {
            return !content.isEmpty() ? content.charAt(0) : '\0';
        }
        throw new IllegalArgumentException("Unsupported primitive or wrapper type: " + targetClass.getName());
    }
}
