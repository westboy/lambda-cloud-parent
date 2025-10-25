package com.lambda.cloud.iotdb;

import com.lambda.cloud.iotdb.handler.MessageHandler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.iotdb.session.subscription.consumer.ISubscriptionTablePullConsumer;
import org.apache.iotdb.session.subscription.consumer.table.SubscriptionTablePullConsumerBuilder;
import org.apache.iotdb.session.subscription.payload.SubscriptionMessage;
import org.apache.tsfile.read.common.RowRecord;

/**
 * IotDbConsumerContainer
 *
 * @author Jin
 */
public class IotDbConsumerContainer {

    private final String consumerId;
    private final String topic;
    private final String consumerGroupId;

    private final List<MessageHandler> listeners = new CopyOnWriteArrayList<>();
    private ISubscriptionTablePullConsumer pullConsumer;

    public IotDbConsumerContainer(String consumerId, String topic, String consumerGroupId) {
        this.consumerId = consumerId;
        this.topic = topic;
        this.consumerGroupId = consumerGroupId;
    }

    public void addListener(MessageHandler handler) {
        listeners.add(handler);
    }

    public void start() {
        pullConsumer = new SubscriptionTablePullConsumerBuilder()
                .consumerId(consumerId)
                .consumerGroupId(consumerGroupId)
                .username("root")
                .password("<PASSWORD>")
                .buildTablePullConsumer();
        pullConsumer.open();
        pullConsumer.subscribe(topic);
        new Thread(this::pollMessages).start();
    }

    private void pollMessages() {
        while (true) {
            List<SubscriptionMessage> messages = pullConsumer.poll(10000);
            for (SubscriptionMessage message : messages) {
                for (var dataSet : message.getSessionDataSetsHandler()) {
                    while (dataSet.hasNext()) {
                        RowRecord record = dataSet.next();
                        listeners.forEach(handler -> handler.handle(record));
                    }
                }
            }
        }
    }

    public void stop() throws Exception {
        if (pullConsumer != null) {
            pullConsumer.unsubscribe();
            pullConsumer.close();
        }
    }
}
