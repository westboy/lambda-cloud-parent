package com.jingfang.core.jackson2;

import java.time.format.DateTimeFormatter;

/**
 * @author Jin
 */
@SuppressWarnings("squid:S110")
public class JingfangLocalDateTimeSerializer extends com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer {

    public JingfangLocalDateTimeSerializer() {
        super(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}