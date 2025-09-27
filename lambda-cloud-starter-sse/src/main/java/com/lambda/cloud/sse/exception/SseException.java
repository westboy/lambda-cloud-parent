package com.lambda.cloud.sse.exception;

import lombok.Getter;

/**
 * SSE操作异常基类
 */
@Getter
public class SseException extends RuntimeException {
    private final String clientId;
    private final String eventName;

    public SseException(String message) {
        super(message);
        this.clientId = null;
        this.eventName = null;
    }

    public SseException(String message, Throwable cause) {
        super(message, cause);
        this.clientId = null;
        this.eventName = null;
    }

    public SseException(String clientId, String eventName, String message) {
        super(message);
        this.clientId = clientId;
        this.eventName = eventName;
    }

    public SseException(String clientId, String eventName, String message, Throwable cause) {
        super(message, cause);
        this.clientId = clientId;
        this.eventName = eventName;
    }

}
