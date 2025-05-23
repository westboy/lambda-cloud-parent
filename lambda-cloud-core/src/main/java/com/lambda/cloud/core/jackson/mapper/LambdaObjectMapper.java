package com.lambda.cloud.core.jackson.mapper;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lambda.cloud.core.jackson.deserializer.LambdaCloudLocalDateTimeDeserializer;
import com.lambda.cloud.core.jackson.serializer.LambdaCloudLocalDateTimeSerializer;
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
        this.activateDefaultTyping(getPolymorphicTypeValidator(), NON_FINAL, PROPERTY);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LambdaCloudLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LambdaCloudLocalDateTimeDeserializer());
        this.registerModule(javaTimeModule);
    }

    @Override
    public ObjectMapper copy() {
        return new LambdaObjectMapper();
    }
}
