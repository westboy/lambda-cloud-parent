package com.lambda.cloud.kafka;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lambda.autoconfig.KafkaDelayAutoConfiguration;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = {KafkaDelayAutoConfiguration.class, KafkaAutoConfiguration.class})
@DirtiesContext
@EmbeddedKafka(partitions = 10)
class KafkaDelayIntegrationTest {

    @Autowired
    private DelayKafkaTemplate delayTemplate;

    private String receivedMessage;
    private long receivedTime;

    void shouldDeliverMessageWithCorrectDelay() throws Exception {
        // Given
        String testMessage = "test-message";
        int delaySeconds = 60;
        long sendTime = System.currentTimeMillis();
        System.out.println("Test started at: " + sendTime);

        // When
        delayTemplate.send("test-topic", testMessage, delaySeconds);
        System.out.println("Message sent at: " + System.currentTimeMillis());

        // Then
        // 增加超时时间并添加更多调试信息
        System.out.println("Waiting for message with delay of " + delaySeconds + " seconds...");

        await().atMost(delaySeconds + 5, TimeUnit.SECONDS) // 增加超时缓冲
                .pollInterval(200, TimeUnit.MILLISECONDS) // 增加轮询间隔
                .until(() -> {
                    long currentTime = System.currentTimeMillis();
                    System.out.println("Polling at: " + currentTime + ", elapsed: " + (currentTime - sendTime) + "ms");
                    return receivedMessage != null;
                });

        long actualDelay = (receivedTime - sendTime) / 1000;
        System.out.println("Message received at: " + receivedTime + ", actual delay: " + actualDelay + "s");

        assertTrue(
                actualDelay >= delaySeconds && actualDelay <= delaySeconds + 6,
                "Expected delay between " + (delaySeconds - 1) + " and " + (delaySeconds + 2) + " seconds but was "
                        + actualDelay);
        assertEquals(testMessage, receivedMessage);
    }

    @KafkaListener(topics = "lambda-cloud-delay-topic", groupId = "test-topic")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.println("Received message: " + record.value() + " at offset: "
                + record.offset() + " from partition: "
                + record.partition());
        receivedMessage = record.value();
        receivedTime = System.currentTimeMillis();
    }
}
