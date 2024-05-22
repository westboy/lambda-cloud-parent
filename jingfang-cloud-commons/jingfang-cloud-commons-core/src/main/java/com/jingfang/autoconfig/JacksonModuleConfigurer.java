package com.jingfang.autoconfig;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jingfang.cloud.core.jackson2.DefaultLocalDateTimeDeserializer;
import com.jingfang.cloud.core.jackson2.DefaultLocalDateTimeSerializer;
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
        javaTimeModule.addSerializer(LocalDateTime.class, new DefaultLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new DefaultLocalDateTimeDeserializer());
        return javaTimeModule;
    }

}
