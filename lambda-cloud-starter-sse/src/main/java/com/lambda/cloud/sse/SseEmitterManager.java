package com.lambda.cloud.sse;

import com.lambda.autoconfig.SseProperties;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SseEmitterManager
 *
 * @author Jin
 */
public class SseEmitterManager {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final SseProperties properties;
    private final ScheduledExecutorService executorService;
    private final CopyOnWriteArrayList<SseEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService heartbeatExecutor;

    public SseEmitterManager(SseProperties properties) {
        this.properties = properties;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::cleanupExpiredEmitters,
            1, 1, TimeUnit.MINUTES);
        this.heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat,
            0, 30, TimeUnit.SECONDS);
    }

    private void sendHeartbeat() {
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data(System.currentTimeMillis()));
            } catch (IOException e) {
                emitters.remove(clientId);
                listeners.forEach(l -> l.onDisconnect(clientId));
            }
        });
    }

    public void addEventListener(SseEventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(SseEventListener listener) {
        listeners.remove(listener);
    }

    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(properties.getTimeout());
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(clientId);
            listeners.forEach(l -> l.onDisconnect(clientId));
        });
        emitter.onTimeout(() -> {
            emitters.remove(clientId);
            listeners.forEach(l -> l.onDisconnect(clientId));
        });
        emitter.onError(e -> {
            emitters.remove(clientId);
            listeners.forEach(l -> l.onDisconnect(clientId));
        });

        listeners.forEach(l -> l.onConnect(clientId));
        return emitter;
    }

    public void sendEvent(String clientId, String eventName, Object data) throws IOException {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            emitter.send(SseEmitter.event()
                .name(eventName)
                .data(data));
            listeners.forEach(l -> l.onMessageSent(clientId, eventName));
        }
    }

    public void broadcast(String eventName, Object data) {
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
                listeners.forEach(l -> l.onMessageSent(clientId, eventName));
            } catch (IOException e) {
                emitters.remove(clientId);
                listeners.forEach(l -> l.onDisconnect(clientId));
            }
        });
    }

    private void cleanupExpiredEmitters() {
        emitters.entrySet().removeIf(entry -> {
            if (entry.getValue() == null) {
                listeners.forEach(l -> l.onDisconnect(entry.getKey()));
                return true;
            }
            return false;
        });
    }

    public void close() {
        executorService.shutdown();
        heartbeatExecutor.shutdown();
        emitters.forEach((clientId, emitter) -> {
            emitter.complete();
            listeners.forEach(l -> l.onDisconnect(clientId));
        });
        emitters.clear();
    }

    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
