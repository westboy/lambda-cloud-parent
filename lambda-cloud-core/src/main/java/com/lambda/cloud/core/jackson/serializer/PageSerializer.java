package com.lambda.cloud.core.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

/**
 * PageSerializer
 *
 * @author Jin
 */
@JsonComponent
public class PageSerializer extends JsonSerializer<Page<?>> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void serialize(Page<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        int number = value.getNumber() + 1;
        int size = value.getSize();
        gen.writeStartObject();
        gen.writeNumberField("pageSize", size);
        gen.writeNumberField("pageNum", number);
        gen.writeNumberField("size", size);
        gen.writeNumberField("pages", value.getTotalPages());
        gen.writeNumberField("total", value.getTotalElements());
        gen.writeObjectField("list", value.getContent());
        gen.writeEndObject();
    }
}
