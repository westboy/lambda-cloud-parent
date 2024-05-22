package com.jingfang.cloud.mvc;

import com.jingfang.cloud.core.jackson2.DefaultDateFormat;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * @author Jin
 */
public class StringToDateConverter implements Converter<String, Date> {

    @Override
    @SneakyThrows
    public Date convert(@Nonnull String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        return new DefaultDateFormat().parse(source);
    }
}
