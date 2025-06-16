package com.lambda.cloud.rocketmq.message;

import lombok.Data;
import org.apache.rocketmq.client.apis.message.MessageId;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.java.route.Endpoints;
import org.apache.rocketmq.client.java.route.MessageQueueImpl;

import java.util.Collection;
import java.util.Map;

@Data
public class LambdaMessageView<T> implements MessageView {
    private T body;
    private MessageId messageId;
    private String topic;
    private String tag;
    private String messageGroup;
    private Long deliveryTimestamp;
    private Collection<String> keys;
    private Map<String, String> properties;
    private String bornHost;
    private long bornTimestamp;
    private int deliveryAttempt;
    private MessageQueueImpl messageQueue;
    private Endpoints endpoints;
    private String receiptHandle;
    private long offset;
    private boolean corrupted;
    private long decodeTimestamp;
    private Long transportDeliveryTimestamp;
}