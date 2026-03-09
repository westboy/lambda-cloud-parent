package com.lambda.autoconfig;

import com.lambda.cloud.rocketmq.support.LambdaRocketMQMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.support.RocketMQMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * RocketMqAutoConfiguration
 *
 * @author jpjoo
 */
@Slf4j
@AutoConfiguration
public class RocketMqAutoConfiguration {

    public RocketMqAutoConfiguration() {
        log.info("RocketMqAutoConfiguration init");
    }

    @Bean
    public RocketMQMessageConverter rocketMQMessageConverter() {
        return new LambdaRocketMQMessageConverter();
    }
}
