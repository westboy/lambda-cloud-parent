package com.lambda.cloud.core.jackson.serializer;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.format.DateTimeFormatter;

/**
 * CustomLocalDateTimeSerializer
 *
 * @author Jin
 */
@SuppressWarnings("all")
public class LambdaCloudLocalDateTimeSerializer extends LocalDateTimeSerializer {

    public LambdaCloudLocalDateTimeSerializer() {
        super(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
