package com.jingfang.autoconfig;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jingfang.cloud.core.jackson.dser.CustomLocalDateTimeDeserializer;
import com.jingfang.cloud.core.jackson.ser.CustomLocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @author Jin
 */
@Configuration(proxyBeanMethods = false)
public class JacksonModuleConfigurer {

    @Bean
    public JavaTimeModule javaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        return javaTimeModule;
    }

}
