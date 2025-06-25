package com.lambda.cloud.iotdb.manager;

import com.lambda.cloud.iotdb.IotDbConsumerContainer;
import com.lambda.cloud.iotdb.annotation.IotDbSubscription;
import com.lambda.cloud.iotdb.handler.MessageHandler;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;

/**
 * IotDbMessageListenerInvoker
 *
 * @author Jin
 */
public class IotDbConsumerManager implements ApplicationContextAware {

    private ApplicationContext context;
    private final Map<String, IotDbConsumerContainer> containers = new HashMap<>();

    public void register(Class<?> listenerClass, IotDbSubscription annotation) throws Exception {
        String consumerId = annotation.consumerId();
        if (containers.containsKey(consumerId)) return;

        IotDbConsumerContainer container =
                new IotDbConsumerContainer(annotation.consumerId(), annotation.topic(), annotation.consumerGroupId());

        if (MessageHandler.class.isAssignableFrom(listenerClass)) {
            MessageHandler handler = (MessageHandler) context.getBean(listenerClass);
            container.addListener(handler);
        }

        container.start();
        containers.put(consumerId, container);
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public void stopAll() {
        containers.values().forEach(container -> {
            try {
                container.stop();
            } catch (Exception ignored) {
            }
        });
    }
}
