package com.lambda.cloud.rocketmq.listener;

import static com.lambda.cloud.core.Constants.GSON;

import com.lambda.cloud.rocketmq.message.LambdaMessageView;
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

public abstract class LambdaRocketMQListener<T extends Serializable> implements RocketMQListener {

    private static final Map<Class<?>, Type> TYPE_CACHE = new ConcurrentHashMap<>();

    private Type resolveType() {
        return TYPE_CACHE.computeIfAbsent(getClass(), clazz -> {
            Type superClass = clazz.getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                return ((ParameterizedType) superClass).getActualTypeArguments()[0];
            }
            return String.class;
        });
    }

    public abstract ConsumeResult consume(LambdaMessageView<T> messageView);

    @Override
    public ConsumeResult consume(MessageView messageView) {
        ByteBuffer body = messageView.getBody();
        byte[] bytes = new byte[body.remaining()];
        body.get(bytes);
        T data = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), resolveType());
        LambdaMessageView<T> lambdaMessageView = getLambdaMessageView(messageView, data);
        return consume(lambdaMessageView);
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
