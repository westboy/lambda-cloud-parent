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
 * Jackson模块配置类
 * <p>
 * 该配置类用于自定义Jackson的序列化和反序列化行为，主要包括：
 * <ul>
 *     <li>Java 8时间类型的自定义序列化/反序列化</li>
 *     <li>Long类型转换为字符串以避免JavaScript精度丢失</li>
 * </ul>
 *
 * <p>使用 {@code proxyBeanMethods = false} 来提高性能，因为Bean方法之间没有依赖关系。
 *
 * <p>该配置会自动注册到Spring的Jackson ObjectMapper中，影响全局的JSON处理行为。
 *
 * @author Jin
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class JacksonModuleConfigurer {

    /**
     * 配置Java时间模块
     * <p>
     * 创建并配置JavaTimeModule，用于处理Java 8时间类型的序列化和反序列化。
     * 使用自定义的序列化器和反序列化器来确保时间格式的一致性。
     *
     * @return 配置好的JavaTimeModule实例
     */
    @Bean
    public JavaTimeModule javaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LambdaLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LambdaLocalDateTimeDeserializer());
        return javaTimeModule;
    }

    /**
     * 配置简单模块
     * <p>
     * 创建并配置SimpleModule，主要用于处理Long类型的序列化。
     * 将Long类型序列化为字符串，避免在JavaScript中因精度限制导致的数值错误。
     *
     * <p>JavaScript的Number类型基于IEEE 754双精度浮点数，
     * 只能安全表示-(2^53-1)到2^53-1之间的整数。
     *
     * @return 配置好的SimpleModule实例
     */
    @Bean
    public SimpleModule simpleModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return simpleModule;
    }
}
