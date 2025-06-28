package com.lambda.cloud.core;

import com.google.gson.Gson;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

/**
 * @author w
 */
@UtilityClass
public final class Constants {

    /**
     * 加密方式
     */
    public static final String HMAC = "hmac";

    /**
     * Gson
     */
    public static final Gson GSON = new Gson();

    /**
     * 日期和时间格式
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

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
    public static final String ISO_DATE_TIME_PATTERN = DATE_TIME_PATTERN + "T " + TIME_PATTERN;

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

    public static final String ISO8601_REGEX = DATE_REGEX + "T" + TIME_REGEX + "\\.\\d{3}Z";

    public static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
}
