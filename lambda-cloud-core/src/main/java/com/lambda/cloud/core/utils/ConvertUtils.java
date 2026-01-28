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

    @SuppressWarnings("unchecked")
    public static <T, S> Object convertSmart(S source) {
        if (source == null) {
            return null;
        }
        if (source instanceof List<?> list) {
            if (list.isEmpty()) {
                return List.of();
            }
            BaseConverter<S, T> converter =
                    ConverterResolver.getConverter(list.getFirst().getClass());

            return converter.convertToList((List<S>) list);
        }
        return convert(source);
    }
}
