package com.lambda.cloud.mvc;

import com.lambda.cloud.core.jackson.text.ExtendDateFormat;
import java.util.Date;
import javax.annotation.Nonnull;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;

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
		return new ExtendDateFormat().parse(source);
	}
}
