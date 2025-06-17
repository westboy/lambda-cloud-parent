package com.lambda.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.rocketmq.support.LambdaRocketMQMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.support.RocketMQMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMqAutoConfiguration
 *
 * @author jpjoo
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class RocketMqAutoConfiguration {

    public RocketMqAutoConfiguration() {
        log.info("RocketMqAutoConfiguration init");
    }

    @Bean
    public RocketMQMessageConverter rocketMQMessageConverter(ObjectMapper objectMapper) {
        return new LambdaRocketMQMessageConverter(objectMapper);
    }
}
