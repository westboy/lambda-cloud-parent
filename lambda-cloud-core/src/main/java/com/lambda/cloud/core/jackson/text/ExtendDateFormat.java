package com.lambda.cloud.core.jackson.text;

import static com.lambda.cloud.core.Constants.*;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ArrayUtil;
import com.lambda.cloud.core.exception.NotSupportedException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jin
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class ExtendDateFormat extends SimpleDateFormat {

    private static final String[] EXCLUDES = new String[] {"0001-01-01T00:00:00Z"};

    public ExtendDateFormat() {
        super(DATE_TIME_PATTERN);
    }

    @Override
    public Date parse(@Nonnull String source) {
        ParsePosition pos = new ParsePosition(0);
        return parse(source, pos);
    }

    @Override
    @SneakyThrows
    public Date parse(@Nonnull String source, @Nonnull ParsePosition pos) {
        if (ArrayUtil.contains(EXCLUDES, source)) {
            return null;
        }
        if (source.matches(TIME_STAMP_REGEX)) {
            long timestamp = Long.parseLong(source);
            DateTime date = new DateTime(timestamp);
            return date.toJdkDate();
        }
        java.text.DateFormat format;
        if (source.matches(DATE_TIME_REGEX)) {
            format = new SimpleDateFormat(DATE_TIME_PATTERN);
        } else if (source.matches(DATE_REGEX)) {
            format = new SimpleDateFormat(DATE_PATTERN);
        } else if (source.matches(YEAR_MONTH_REGEX)) {
            format = new SimpleDateFormat(YEAR_MONTH_PATTERN);
        } else if (source.matches(ISO8601_REGEX)) {
            format = new SimpleDateFormat(ISO8601_PATTERN);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
        } else {
            throw new NotSupportedException("source: " + source);
        }
        return format.parse(source, pos);
    }
}
