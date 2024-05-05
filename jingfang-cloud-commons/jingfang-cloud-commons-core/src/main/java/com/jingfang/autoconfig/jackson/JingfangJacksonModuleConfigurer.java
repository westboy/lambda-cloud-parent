package com.jingfang.autoconfig.jackson;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jingfang.core.jackson2.JingfangLocalDateTimeDeserializer;
import com.jingfang.core.jackson2.JingfangLocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @author Jin
 */
@Configuration(proxyBeanMethods = false)
public class JingfangJacksonModuleConfigurer {

    @Bean
    public JavaTimeModule javaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new JingfangLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new JingfangLocalDateTimeDeserializer());
        return javaTimeModule;
    }


}
