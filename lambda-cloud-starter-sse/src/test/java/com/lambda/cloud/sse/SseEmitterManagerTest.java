package com.lambda.cloud.sse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lambda.autoconfig.SseProperties;
import java.io.IOException;

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
    void testCreateEmitter() {
        SseEmitter emitter = manager.createEmitter("client1");
        assertNotNull(emitter);
        assertEquals(1, manager.getActiveConnectionCount());
    }

    @Test
    void testSendEvent() throws IOException {
        SseEmitter emitter = manager.createEmitter("client1");
        manager.sendEvent("client1", "testEvent", "testData");
        assertEquals(1, manager.getActiveConnectionCount());
    }

    @Test
    void testBroadcast() {
        manager.createEmitter("client1");
        manager.createEmitter("client2");
        manager.broadcast("testEvent", "testData");
        assertEquals(2, manager.getActiveConnectionCount());
    }

    @Test
    void testEventListener() {
        SseEventListener listener = mock(SseEventListener.class);
        manager.addEventListener(listener);

        SseEmitter emitter = manager.createEmitter("client1");
        verify(listener).onConnect("client1");

        manager.sendEvent("client1", "testEvent", "testData");
        verify(listener).onMessageSent("client1", "testEvent");

        emitter.complete();
        verify(listener).onDisconnect("client1");
    }

    @Test
    void testClose() {
        manager.createEmitter("client1");
        manager.createEmitter("client2");
        manager.shutdown();
        assertEquals(0, manager.getActiveConnectionCount());
    }
}
