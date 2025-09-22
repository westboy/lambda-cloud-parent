package com.lambda.cloud.core.shared;

import cn.hutool.extra.spring.SpringUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConverterResolver {

    private static final Map<Class<?>, BaseConverter<?, ?>> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    protected <DV, ET> BaseConverter<DV, ET> getConverter(Class<?> clazz) {
        return (BaseConverter<DV, ET>) CACHE.computeIfAbsent(clazz, cls -> {
            try {
                String converterClassName = cls.getName() + "Converter";
                Class<?> converterClass = Class.forName(converterClassName);
                if (!BaseConverter.class.isAssignableFrom(converterClass)) {
                    throw new IllegalStateException("Class " + converterClassName + " is not a BaseConverter");
                }
                return (BaseConverter<?, ?>) SpringUtil.getBean(converterClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Converter not found for " + cls.getName(), e);
            }
        });
    }
}
