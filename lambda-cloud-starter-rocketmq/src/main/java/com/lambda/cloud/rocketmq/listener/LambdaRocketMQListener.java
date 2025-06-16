package com.lambda.cloud.rocketmq.listener;

import com.google.gson.Gson;
import com.lambda.cloud.rocketmq.message.LambdaMessageView;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

public abstract class LambdaRocketMQListener<T extends Serializable> implements RocketMQListener {
    private static final Gson GSON = new Gson();
    private final Type type;

    public LambdaRocketMQListener() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("泛型类型不能为空");
        }
    }

    public abstract ConsumeResult consume(LambdaMessageView<T> messageView);

    @Override
    public ConsumeResult consume(MessageView messageView) {
        ByteBuffer body = messageView.getBody();
        byte[] bytes = new byte[body.remaining()];
        body.get(bytes);
        T data = GSON.fromJson(new String(bytes), type);
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
        return consume(lambdaMessageView);
    }
}
