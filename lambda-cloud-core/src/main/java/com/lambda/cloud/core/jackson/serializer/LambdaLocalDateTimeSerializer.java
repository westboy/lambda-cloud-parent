package com.lambda.cloud.core.jackson.serializer;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.lambda.cloud.core.Constants;

/**
 * CustomLocalDateTimeSerializer
 *
 * @author Jin
 */
@SuppressWarnings("all")
public class LambdaLocalDateTimeSerializer extends LocalDateTimeSerializer {

    public LambdaLocalDateTimeSerializer() {
        super(Constants.YYYY_MM_DD_HH_MM_SS_FORMATTER);
    }
}
