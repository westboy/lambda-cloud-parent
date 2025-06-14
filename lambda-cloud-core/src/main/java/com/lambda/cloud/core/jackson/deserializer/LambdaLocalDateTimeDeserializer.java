package com.lambda.cloud.core.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * CustomLocalDateTimeDeserializer
 *
 * @author Jin
 */
@SuppressWarnings("all")
public class LambdaLocalDateTimeDeserializer extends LocalDateTimeDeserializer {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd[[' 'HH][:mm][:ss]]")
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            .toFormatter();

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            long value = parser.getValueAsLong();
            Instant instant = Instant.ofEpochMilli(value);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } else if (parser.hasTokenId(JsonTokenId.ID_STRING)) {
            String string = parser.getText();
            if (string.isEmpty()) {
                return !this.isLenient() ? this._failForNotLenient(parser, context, JsonToken.VALUE_STRING) : null;
            } else {
                try {
                    if (string.length() > 10 && string.charAt(10) == 'T') {
                        return string.endsWith("Z")
                                ? LocalDateTime.ofInstant(Instant.parse(string), ZoneOffset.UTC)
                                : LocalDateTime.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } else {
                        return LocalDateTime.parse(string, formatter);
                    }
                } catch (DateTimeException var12) {
                    return super.deserialize(parser, context);
                }
            }
        }
        return super.deserialize(parser, context);
    }
}
