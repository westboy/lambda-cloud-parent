package com.lambda.cloud.core.utils;

import com.lambda.cloud.core.convert.BaseConverter;
import com.lambda.cloud.core.convert.ConverterResolver;
import java.util.List;

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

    public static <T, S> List<T> convertList(List<S> source) {
        if (source == null) {
            return null;
        }
        if (source.isEmpty()) {
            return List.of();
        }
        S sourceFirst = source.getFirst();
        BaseConverter<S, T> converter = ConverterResolver.getConverter(sourceFirst.getClass());

        return converter.convertToList(source);
    }
}
