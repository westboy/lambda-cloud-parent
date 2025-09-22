package com.lambda.cloud.core.resolver;

import cn.hutool.extra.spring.SpringUtil;
import com.lambda.cloud.core.convert.BaseConverter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConverterResolver {

    private static final Map<Class<?>, BaseConverter<?, ?>> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <D, E> BaseConverter<D, E> getConverter(Class<?> clazz) {
        return (BaseConverter<D, E>) CACHE.computeIfAbsent(clazz, cls -> {
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
