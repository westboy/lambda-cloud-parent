package com.lambda.autoconfig;

import static org.apache.rocketmq.client.autoconfigure.RocketMQAutoConfiguration.*;

import com.lambda.cloud.rocketmq.client.LambdaRocketMQClientTemplate;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumerBuilder;
import org.apache.rocketmq.client.apis.producer.ProducerBuilder;
import org.apache.rocketmq.client.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.apache.rocketmq.client.support.RocketMQMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jin
 */
@Slf4j
@Configuration
@AutoConfigureAfter({RocketMQAutoConfiguration.class})
public class RocketMqAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("All")
    @Bean(destroyMethod = "destroy")
    public RocketMQClientTemplate rocketMQClientTemplate(RocketMQMessageConverter rocketMQMessageConverter) {
        LambdaRocketMQClientTemplate rocketMQClientTemplate = new LambdaRocketMQClientTemplate();

        if (applicationContext.containsBean(PRODUCER_BUILDER_BEAN_NAME)) {
            rocketMQClientTemplate.setProducerBuilder(
                    (ProducerBuilder) applicationContext.getBean(PRODUCER_BUILDER_BEAN_NAME));
        }
        if (applicationContext.containsBean(SIMPLE_CONSUMER_BUILDER_BEAN_NAME)) {
            rocketMQClientTemplate.setSimpleConsumerBuilder(
                    (SimpleConsumerBuilder) applicationContext.getBean(SIMPLE_CONSUMER_BUILDER_BEAN_NAME));
        }
        rocketMQClientTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        return rocketMQClientTemplate;
    }
}
