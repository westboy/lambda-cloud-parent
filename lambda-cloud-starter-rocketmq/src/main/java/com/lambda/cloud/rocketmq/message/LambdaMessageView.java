package com.lambda.cloud.rocketmq.message;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import org.apache.rocketmq.client.apis.message.MessageId;

import java.util.Collection;
import java.util.Map;

@SuppressFBWarnings({"EI_EXPOSE_REP"})
@Data
public class LambdaMessageView<T> {
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
    private String receiptHandle;
    private long offset;
    private boolean corrupted;
    private long decodeTimestamp;
    private Long transportDeliveryTimestamp;
}
