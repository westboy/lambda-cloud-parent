package com.lambda.cloud.sse.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

    SseEmitter createEmitter(String clientId);
    SseEmitter createEmitter(String clientId,Object payload);
    void sendEvent(String clientId, String eventName, Object payload);
    void broadcast(String eventName, Object data);
}
