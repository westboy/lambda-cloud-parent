package com.jingfang.autoconfig.jackson;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jingfang.cloud.core.jackson2.JFLocalDateTimeDeserializer;
import com.jingfang.cloud.core.jackson2.JFLocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @author Jin
 */
@Configuration(proxyBeanMethods = false)
public class JFJacksonModuleConfigurer {

    @Bean
    public JavaTimeModule javaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new JFLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new JFLocalDateTimeDeserializer());
        return javaTimeModule;
    }


}
