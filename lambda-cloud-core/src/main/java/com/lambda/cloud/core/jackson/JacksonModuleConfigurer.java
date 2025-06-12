package com.lambda.cloud.core.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lambda.cloud.core.jackson.deserializer.LambdaLocalDateTimeDeserializer;
import com.lambda.cloud.core.jackson.serializer.LambdaLocalDateTimeSerializer;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jin
 */
@Configuration(proxyBeanMethods = false)
public class JacksonModuleConfigurer {

	@Bean
	public JavaTimeModule javaTimeModule() {
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalDateTime.class, new LambdaLocalDateTimeSerializer());
		javaTimeModule.addDeserializer(LocalDateTime.class, new LambdaLocalDateTimeDeserializer());
		return javaTimeModule;
	}

	@Bean
	public SimpleModule simpleModule() {
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		return simpleModule;
	}
}
