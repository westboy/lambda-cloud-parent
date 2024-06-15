package com.jingfang.autoconfig;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    // 单例bean声明
    private static JavaTimeModule javaTimeModuleInstance;
    private static SimpleModule simpleModuleInstance;

    @Bean
    public JavaTimeModule javaTimeModule() {
        if (javaTimeModuleInstance == null) {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
            javaTimeModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
            javaTimeModuleInstance = javaTimeModule;
        }
        return javaTimeModuleInstance;
    }

    @Bean
    public SimpleModule simpleModule() {
        if (simpleModuleInstance == null) {
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            simpleModuleInstance = simpleModule;
        }
        return simpleModuleInstance;
    }
}
