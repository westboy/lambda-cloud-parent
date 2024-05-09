package com.jingfang.cloud.core.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * @author Jin
 */
public class JingfangObjectMapper extends ObjectMapper {

    public JingfangObjectMapper() {
        super();
        this.setDateFormat(new JingfangDateFormat());
        this.disable(INDENT_OUTPUT);
        this.setSerializationInclusion(NON_NULL);
        this.setSerializationInclusion(NON_EMPTY);
        this.activateDefaultTyping(getPolymorphicTypeValidator(), NON_FINAL, PROPERTY);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new JingfangLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new JingfangLocalDateTimeDeserializer());
        this.registerModule(javaTimeModule);
    }


    @Override
    public com.fasterxml.jackson.databind.ObjectMapper copy() {
        return new JingfangObjectMapper();
    }
}
