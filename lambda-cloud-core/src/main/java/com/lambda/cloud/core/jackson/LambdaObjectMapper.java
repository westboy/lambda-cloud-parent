package com.lambda.cloud.core.jackson;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lambda.cloud.core.jackson.deserializer.LambdaLocalDateTimeDeserializer;
import com.lambda.cloud.core.jackson.serializer.LambdaLocalDateTimeSerializer;
import com.lambda.cloud.core.jackson.text.ExtendDateFormat;
import java.time.LocalDateTime;

/**
 * @author Jin
 */
public class LambdaObjectMapper extends ObjectMapper {

    public LambdaObjectMapper() {
        super();
        this.setDateFormat(new ExtendDateFormat());
        this.disable(INDENT_OUTPUT);
        this.setSerializationInclusion(NON_NULL);
        this.setSerializationInclusion(NON_EMPTY);
        final JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LambdaLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LambdaLocalDateTimeDeserializer());
        this.registerModule(javaTimeModule);
        final PolymorphicTypeValidator polymorphicTypeValidator = getPolymorphicTypeValidator();
        this.activateDefaultTyping(polymorphicTypeValidator, NON_FINAL, PROPERTY);
    }

    @Override
    public ObjectMapper copy() {
        return new LambdaObjectMapper();
    }
}
