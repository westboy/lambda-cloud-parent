package com.lambda.cloud.sse.service;

import com.lambda.cloud.sse.SseEmitterManager;
import com.lambda.cloud.sse.initializer.SseEmitterInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final SseEmitterManager emitterManager;
    private final SseEmitterInitializer sseEmitterInitializer;

    @Override
    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = emitterManager.createEmitter(clientId);
        sseEmitterInitializer.initialize(emitter);
        return emitter;
    }

    @Override
    public SseEmitter createEmitter(String clientId, Object payload) {
        SseEmitter emitter = emitterManager.createEmitter(clientId);
        sseEmitterInitializer.initialize(emitter, payload);
        return emitter;
    }

    @Override
    public void sendEvent(String clientId, String eventName, Object payload) {
        try {
            emitterManager.sendEvent(clientId, eventName, payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SSE event", e);
        }
    }

    @Override
    public void broadcast(String eventName, Object data) {
        emitterManager.broadcast(eventName, data);
    }
}
