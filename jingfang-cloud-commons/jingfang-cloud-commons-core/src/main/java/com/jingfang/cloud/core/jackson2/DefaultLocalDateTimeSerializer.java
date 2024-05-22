package com.jingfang.cloud.core.jackson2;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.format.DateTimeFormatter;

/**
 * @author Jin
 */
@SuppressWarnings("squid:S110")
public class DefaultLocalDateTimeSerializer extends LocalDateTimeSerializer {

    public DefaultLocalDateTimeSerializer() {
        super(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}