package com.lambda.cloud.sse;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lambda.autoconfig.SseProperties;
import com.lambda.cloud.sse.listener.SseEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterManagerTest {
    private SseEmitterManager manager;

    @BeforeEach
    void setUp() {
        SseProperties properties = new SseProperties();
        properties.setTimeout(30000L);
        manager = new SseEmitterManager(properties);
    }

    @Test
    void testEventListener() {
        SseEventListener listener = mock(SseEventListener.class);
        manager.addEventListener(listener);

        SseEmitter emitter = manager.createEmitter("client1");
        verify(listener).onConnect("client1");

        manager.sendEvent("client1", "testEvent", "testData");
        await().untilAsserted(() -> verify(listener).onMessageSent("client1", "testEvent"));
        emitter.complete();

        manager.removeEmitter("client1");
        verify(listener).onDisconnect("client1");
    }
}
