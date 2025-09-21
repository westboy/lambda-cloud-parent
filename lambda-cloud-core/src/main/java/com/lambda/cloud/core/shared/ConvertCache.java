package com.lambda.cloud.core.shared;

import cn.hutool.extra.spring.SpringUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConvertCache<D, E> {

    private static final Map<Class<?>, BaseConverter<?, ?>> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    protected BaseConverter<D, E> getConverter(Class<?> clazz) {
        return (BaseConverter<D, E>) CACHE.computeIfAbsent(clazz, cls -> {
            try {
                Class<?> converterClass = Class.forName(cls.getName() + "Converter");
                return (BaseConverter<?, ?>) SpringUtil.getBean(converterClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Converter not found for " + cls.getName(), e);
            }
        });
    }
}
