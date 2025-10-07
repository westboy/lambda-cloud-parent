package com.lambda.cloud.core.utils;

import com.lambda.cloud.core.convert.BaseConverter;
import com.lambda.cloud.core.convert.ConverterResolver;

/**
 * ConvertUtils
 *
 * @author Jin
 */
public class ConvertUtils {

    public static <T, S> T convert(S source) {
        BaseConverter<S, T> converter = ConverterResolver.getConverter(source.getClass());
        return converter.convertTo(source);
    }
}
