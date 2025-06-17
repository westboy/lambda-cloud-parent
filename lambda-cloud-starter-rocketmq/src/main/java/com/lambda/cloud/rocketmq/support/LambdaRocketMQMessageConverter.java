package com.lambda.cloud.rocketmq.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.client.support.RocketMQMessageConverter;
import org.springframework.messaging.converter.*;

public class LambdaRocketMQMessageConverter extends RocketMQMessageConverter {

    private final MessageConverter messageConverter;

    public LambdaRocketMQMessageConverter(ObjectMapper objectMapper) {
        List<MessageConverter> messageConverters = new ArrayList<>();
        ByteArrayMessageConverter byteArrayMessageConverter = new ByteArrayMessageConverter();
        byteArrayMessageConverter.setContentTypeResolver(null);
        messageConverters.add(byteArrayMessageConverter);
        messageConverters.add(new StringMessageConverter());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        messageConverters.add(converter);
        messageConverter = new CompositeMessageConverter(messageConverters);
    }

    @Override
    public MessageConverter getMessageConverter() {
        return messageConverter;
    }
}
