package com.lambda.cloud.sse.controller;

import com.lambda.cloud.sse.service.SseService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SseController
 *
 * @author Jin
 */
@RestController
@RequestMapping("${lambda.sse.endpoint-prefix:/sse}")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(value = "${lambda.sse.subscribe-path:/subscribe}/{clientId}")
    public SseEmitter subscribe(@PathVariable String clientId) {
        return sseService.createEmitter(clientId);
    }

    @PostMapping(value = "${lambda.sse.subscribe-path:/subscribe}/{clientId}")
    public SseEmitter subscribe(@PathVariable String clientId, @RequestBody Object payload) {
        return sseService.createEmitter(clientId, payload);
    }

    @PostMapping("${lambda.sse.send-path:/send}/{clientId}/{eventName}")
    public void sendEvent(@PathVariable String clientId, @PathVariable String eventName, @RequestBody Object payload) {
        sseService.sendEvent(clientId, eventName, payload);
    }

    @PostMapping("${lambda.sse.broadcast-path:/broadcast}/{eventName}")
    public void broadcast(@PathVariable String eventName, @RequestBody Object data) {
        sseService.broadcast(eventName, data);
    }
}
