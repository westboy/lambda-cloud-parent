package com.jingfang.cloud.core.jackson2;


import cn.hutool.core.date.DateTime;
import com.jingfang.cloud.core.exception.NotSupportedException;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author jin
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class JFDateFormat extends SimpleDateFormat {
    /**
     * 日期正则表达式
     */
    public static final String DATE_REGEX = "[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])";

    /**
     * 日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 时间正则表达式
     */
    public static final String TIME_REGEX = "(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d";

    /**
     * 时间格式
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 日期和时间正则表达式
     */
    public static final String DATE_TIME_REGEX = DATE_REGEX + "\\s" + TIME_REGEX;

    /**
     * 日期和时间格式
     */
    public static final String DATE_TIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    /**
     * 13位时间戳正则表达式
     */
    public static final String TIME_STAMP_REGEX = "1\\d{12}";

    /**
     * 年和月正则表达式
     */
    public static final String YEAR_MONTH_REGEX = "[1-9]\\d{3}-(0[1-9]|1[0-2])";

    /**
     * 年和月格式
     */
    public static final String YEAR_MONTH_PATTERN = "yyyy-MM";

    private static final String[] EXCLUDES = new String[]{"0001-01-01T00:00:00Z"};

    public static final String ISO8601_REGEX = DATE_REGEX + "T" + TIME_REGEX + "\\.\\d{3}Z";

    public static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";


    public JFDateFormat() {
        super(DATE_TIME_PATTERN);
    }


    @Override
    public Date parse(@Nonnull String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        return parse(source, pos);
    }

    @Override
    @SneakyThrows
    public Date parse(@Nonnull String source, @Nonnull ParsePosition pos) {
        if (ArrayUtils.contains(EXCLUDES, source)) {
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
