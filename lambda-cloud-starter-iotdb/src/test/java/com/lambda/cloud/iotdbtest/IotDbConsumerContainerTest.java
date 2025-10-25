package com.lambda.cloud.iotdbtest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lambda.cloud.iotdb.IotDbConsumerContainer;
import com.lambda.cloud.iotdb.handler.MessageHandler;
import org.apache.tsfile.read.common.RowRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IotDbConsumerContainerTest {

    private IotDbConsumerContainer container;
    private MessageHandler mockHandler;

    @BeforeEach
    void setUp() {
        container = new IotDbConsumerContainer("test-consumer", "test-topic", "test-group", "user", "pass", 5000L, 10);
        mockHandler = mock(TestHandler.class);
    }

    @Test
    void constructorAndGetters() {
        assertEquals("test-consumer", container.getConsumerId());
        assertEquals("test-topic", container.getTopic());
        assertEquals("test-group", container.getConsumerGroupId());
        assertEquals(IotDbConsumerContainer.ContainerState.CREATED, container.getState());
        assertFalse(container.isRunning());
        assertEquals(0, container.getListenerCount());
    }

    @Test
    void addListenerWorks() {
        container.addListener(mockHandler);
        assertEquals(1, container.getListenerCount());
    }

    @Test
    void addNullListenerThrows() {
        assertThrows(IllegalArgumentException.class, () -> container.addListener(null));
    }

    @Test
    void stopWithoutStartIsSafe() {
        assertDoesNotThrow(() -> container.stop());
    }

    static class TestHandler implements MessageHandler {
        volatile RowRecord lastRecord;

        @Override
        public void handle(RowRecord record) {
            lastRecord = record;
        }
    }
}
