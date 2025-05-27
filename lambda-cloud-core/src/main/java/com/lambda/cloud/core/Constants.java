package com.lambda.cloud.core;

import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

/**
 * @author w
 */
@UtilityClass
public final class Constants {
    /**
     * date
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
}
