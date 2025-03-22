package com.lamuda.cloud.core.jackson.ser;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.format.DateTimeFormatter;

/**
 * @author Jin
 */
@SuppressWarnings("squid:S110")
public class CustomLocalDateTimeSerializer extends LocalDateTimeSerializer {

    public CustomLocalDateTimeSerializer() {
        super(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}