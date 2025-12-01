package com.lambda.cloud.sse.controller;

import com.lambda.cloud.sse.SseEmitterManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SseController
 *
 * @author Jin
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
@RestController
@RequestMapping("${lambda.sse.endpoint-prefix:/sse}")
public class SseController {

    private final SseEmitterManager emitterManager;

    public SseController(SseEmitterManager emitterManager) {
        this.emitterManager = emitterManager;
    }

    @GetMapping(value = "${lambda.sse.subscribe-path:/subscribe}/{clientId}")
    public SseEmitter subscribe(@PathVariable String clientId) {
        return emitterManager.createEmitter(clientId);
    }

    @PostMapping("${lambda.sse.send-path:/send}/{clientId}/{eventName}")
    public void sendEvent(@PathVariable String clientId, @PathVariable String eventName, @RequestBody Object data) {
        try {
            emitterManager.sendEvent(clientId, eventName, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SSE event", e);
        }
    }

    @PostMapping("${lambda.sse.broadcast-path:/broadcast}/{eventName}")
    public void broadcast(@PathVariable String eventName, @RequestBody Object data) {
        emitterManager.broadcast(eventName, data);
    }
}
