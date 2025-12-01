package com.lambda.cloud.sse;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class WrappedEmitter {
    SseEmitter emitter;
    AtomicBoolean completed = new AtomicBoolean(false);

    public WrappedEmitter(SseEmitter emitter) {
        this.emitter = emitter;
    }

    public Boolean isComplete() {
        return completed.get();
    }

    public void complete() {
        completed.set(true);
    }
}
