package com.lambda.cloud.sse.initializer;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterInitializer {

    <T> void initialize(SseEmitter emitter);

    <T> void initialize(SseEmitter emitter, T payload);

}
