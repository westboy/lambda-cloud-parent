package com.lambda.cloud.sse;

import cn.hutool.core.thread.ThreadUtil;
import com.lambda.autoconfig.SseProperties;
import com.lambda.cloud.sse.listener.SseEventListener;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SseEmitterManager
 *
 * @author Jin
 */
@SuppressWarnings("unused")
@SuppressFBWarnings("EI_EXPOSE_REP2")
@Slf4j
public class SseEmitterManager implements DisposableBean {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final List<SseEventListener> listeners = new CopyOnWriteArrayList<>();
    protected final SseProperties properties;
    protected final ScheduledExecutorService scheduler;
    protected final ExecutorService executor;
    protected final AtomicInteger connectionCount = new AtomicInteger(0);

    protected final AtomicInteger totalMessagesSent = new AtomicInteger(0);
    protected final AtomicInteger failedMessages = new AtomicInteger(0);
    protected final AtomicInteger retryAttempts = new AtomicInteger(0);

    public SseEmitterManager(SseProperties properties) {
        this.properties = properties;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.executor = Executors.newCachedThreadPool();
        startHeartbeatTask();
    }

    protected void startHeartbeatTask() {
        scheduler.scheduleAtFixedRate(
                () -> {
                    broadcast("heartbeat", "ping");
                    log.debug("Sent heartbeat to {} clients", emitters.size());
                },
                properties.getHeartbeatInterval(),
                properties.getHeartbeatInterval(),
                TimeUnit.MILLISECONDS);
    }

    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(properties.getTimeout());
        emitter.onCompletion(() -> removeEmitter(clientId));
        emitter.onTimeout(() -> removeEmitter(clientId));
        emitter.onError(ex -> removeEmitter(clientId));

        SseEmitter oldEmitter = emitters.put(clientId, emitter);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }
        connectionCount.incrementAndGet();

        listeners.forEach(listener -> {
            try {
                listener.onConnect(clientId);
            } catch (Exception e) {
                log.warn("Listener error on connect clientId:{}", clientId);
            }
        });

        return emitter;
    }

    public void sendEvent(String clientId, String eventName, Object data) {
        if (!emitters.containsKey(clientId)) {
            throw new IllegalArgumentException("No emitter found for client: " + clientId);
        }

        executor.submit(() -> {
            SseEmitter emitter = emitters.get(clientId);
            if (emitter == null) {
                return;
            }
            int attempts = 0;
            while (attempts <= properties.getMaxRetryAttempts()) {
                try {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                    totalMessagesSent.incrementAndGet();
                    listeners.forEach(listener -> listener.onMessageSent(clientId, eventName));
                    return;
                } catch (IOException e) {
                    attempts++;
                    if (attempts <= properties.getMaxRetryAttempts()) {
                        retryAttempts.incrementAndGet();
                        log.warn("Retry attempt {} for client {}", attempts, clientId);
                        ThreadUtil.safeSleep(500);
                    } else {
                        failedMessages.incrementAndGet();
                        log.error("Failed to send event after {} attempts", properties.getMaxRetryAttempts(), e);
                        removeEmitter(clientId);
                    }
                }
            }
        });
    }

    public void broadcast(String eventName, Object data) {
        executor.submit(() -> {
            List<String> failedClients = new ArrayList<>();
            emitters.forEach((clientId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                    totalMessagesSent.incrementAndGet();
                    listeners.forEach(listener -> listener.onMessageSent(clientId, eventName));
                } catch (AsyncRequestNotUsableException e) {
                    log.debug("客户端 {} 连接不可用", clientId);
                    failedClients.add(clientId);
                    failedMessages.incrementAndGet();
                } catch (IOException e) {
                    log.warn("客户端 {} IO 异常", clientId);
                    failedClients.add(clientId);
                    failedMessages.incrementAndGet();
                }
            });
            failedClients.forEach(this::removeEmitter);
        });
    }

    public void removeEmitter(String clientId) {
        SseEmitter emitter = emitters.remove(clientId);
        if (emitter != null) {
            connectionCount.decrementAndGet();
            emitter.complete();
            listeners.forEach(listener -> {
                try {
                    listener.onDisconnect(clientId);
                } catch (Exception e) {
                    log.warn("Listener error on disconnect clientId:{}", clientId);
                }
            });
        }
    }

    public Map<String, Object> getStatistics() {
        return Map.of(
                "activeConnections", emitters.size(),
                "totalConnections", connectionCount.get(),
                "totalMessagesSent", totalMessagesSent.get(),
                "failedMessages", failedMessages.get(),
                "retryAttempts", retryAttempts.get(),
                "heartbeatInterval", properties.getHeartbeatInterval());
    }

    public void addEventListener(SseEventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(SseEventListener listener) {
        listeners.remove(listener);
    }

    public int getActiveConnectionCount() {
        return emitters.size();
    }

    public Set<String> getActiveClients() {
        return emitters.keySet();
    }

    public void shutdown() {
        scheduler.shutdown();
        executor.shutdown();
        emitters.values().forEach(SseEmitter::complete);
        emitters.clear();
    }

    @Override
    public void destroy() {
        shutdown();
    }
}
