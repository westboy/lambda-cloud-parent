package com.jingfang.core.utils;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.beans.BeanMap;

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jin
 */
public final class BeanUtils {
    private static final Map<String, BeanCopier> BEAN_COPIER_REPOSITORY = new HashMap<>();

    private BeanUtils() {
    }

    private static Set<String> getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        // @formatter:off
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .collect(Collectors.toSet());
        // @formatter:on
    }

    /**
     * 使用Spring的复制方式
     *
     * @param source 数据来源对象
     * @param target 复制属性到该对象
     */
    public static void copyProperties(Object source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target);
    }

    /**
     * 使用Spring的复制方式
     *
     * @param source  数据来源对象
     * @param target  复制属性到该对象
     * @param ignores 要忽略的属性
     */
    public static void copyProperties(Object source, Object target, Set<String> ignores) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, String.join(",", ignores));
    }

    /**
     * 使用Spring的复制方式
     *
     * @param source       数据来源对象
     * @param target       复制属性到该对象
     * @param excludeNulls 是否排除来源中的空值
     */
    public static void copyProperties(Object source, Object target, boolean excludeNulls) {
        if (excludeNulls) {
            copyProperties(source, target, getNullPropertyNames(source));
        } else {
            copyProperties(source, target);
        }
    }

    /**
     * 使用Cglib的复制方式
     *
     * @param source 数据来源对象
     * @param target 复制属性到该对象
     */
    public static void copyProperties2(Object source, Object target) {
        final Class<?> clazz1 = source.getClass();
        final Class<?> clazz2 = target.getClass();
        String key = generateKey(clazz1, clazz2);
        BeanCopier copier = BEAN_COPIER_REPOSITORY.computeIfAbsent(key, key1 -> BeanCopier.create(clazz1, clazz2, false));
        copier.copy(source, target, null);
    }

    /**
     * map转bean
     *
     * @param map    数据来源map
     * @param target 复制属性到该对象
     */
    public static void mapToBean(Map<String, ?> map, Object target) {
        BeanMap beanMap = BeanMap.create(target);
        beanMap.putAll(map);
    }

    private static String generateKey(Class<?> class1, Class<?> class2) {
        return class1.getName() + class2.getName();
    }
}
