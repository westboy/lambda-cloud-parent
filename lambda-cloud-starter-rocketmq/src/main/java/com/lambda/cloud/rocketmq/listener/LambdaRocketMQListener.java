package com.lambda.cloud.rocketmq.listener;

import com.lambda.cloud.core.utils.ClassTypeUtils;
import com.lambda.cloud.rocketmq.message.LambdaMessageView;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import tools.jackson.databind.ObjectMapper;

public abstract class LambdaRocketMQListener<T extends Serializable>
        implements RocketMQListener, ApplicationContextAware {

    private static final Map<Class<?>, Type> TYPE_CACHE = new ConcurrentHashMap<>();

    public abstract ConsumeResult consume(LambdaMessageView<T> messageView);

    public abstract ConsumeResult consumeError(MessageView messageView, Exception exception);

    private ObjectMapper objectMapper;

    private Type resolveType() {
        return TYPE_CACHE.computeIfAbsent(getClass(), clazz -> {
            Type superClass = clazz.getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                return ((ParameterizedType) superClass).getActualTypeArguments()[0];
            }
            return String.class;
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        objectMapper = applicationContext.getBean(ObjectMapper.class);
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        try {
            byte[] bytes = getBytes(messageView);
            T data = this.convertData(bytes);
            LambdaMessageView<T> lambdaMessageView = getLambdaMessageView(messageView, data);
            return consume(lambdaMessageView);
        } catch (Exception e) {
            return consumeError(messageView, e);
        }
    }

    protected static byte[] getBytes(MessageView messageView) {
        ByteBuffer body = messageView.getBody();
        byte[] bytes = new byte[body.remaining()];
        body.get(bytes);
        return bytes;
    }

    protected static String getString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private T convertData(byte[] bytes) throws IOException {
        Class<T> targetClass;
        Type type = this.resolveType();
        if (type instanceof Class) {
            targetClass = (Class<T>) type;
        } else {
            targetClass = (Class<T>) ((ParameterizedType) type).getRawType();
        }
        if (String.class.equals(targetClass)) {
            return targetClass.cast(getString(bytes));
        }
        if (ClassTypeUtils.isPrimitiveOrWrapper(targetClass)) {
            Object result = ClassTypeUtils.convertPrimitiveOrWrapper(targetClass, getString(bytes));
            return targetClass.cast(result);
        }
        return objectMapper.readValue(bytes, targetClass);
    }

    private static <T extends Serializable> LambdaMessageView<T> getLambdaMessageView(MessageView messageView, T data) {
        LambdaMessageView<T> lambdaMessageView = new LambdaMessageView<>();
        lambdaMessageView.setBody(data);
        lambdaMessageView.setMessageId(messageView.getMessageId());
        lambdaMessageView.setTopic(messageView.getTopic());
        messageView.getTag().ifPresent(lambdaMessageView::setTag);
        messageView.getMessageGroup().ifPresent(lambdaMessageView::setMessageGroup);
        messageView.getDeliveryTimestamp().ifPresent(lambdaMessageView::setDeliveryTimestamp);
        lambdaMessageView.setKeys(messageView.getKeys());
        lambdaMessageView.setProperties(messageView.getProperties());
        lambdaMessageView.setBornHost(messageView.getBornHost());
        lambdaMessageView.setBornTimestamp(messageView.getBornTimestamp());
        lambdaMessageView.setDeliveryAttempt(messageView.getDeliveryAttempt());
        return lambdaMessageView;
    }
}
