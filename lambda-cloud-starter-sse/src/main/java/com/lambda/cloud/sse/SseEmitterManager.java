package com.lambda.cloud.sse;

import com.lambda.autoconfig.SseProperties;
import com.lambda.cloud.sse.listener.SseEventListener;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 安全版 SSE 管理器
 * 自动处理客户端断开、IO 异常
 */
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class SseEmitterManager {

    private final Map<String, WrappedEmitter> emitters = new ConcurrentHashMap<>();
    private final List<SseEventListener> listeners = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final AtomicInteger totalMessagesSent = new AtomicInteger(0);
    private final AtomicInteger failedMessages = new AtomicInteger(0);
    private final AtomicInteger retryAttempts = new AtomicInteger(0);
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    protected final SseProperties properties;

    public SseEmitterManager(SseProperties properties) {
        this.properties = properties;
        startHeartbeat();
    }

    /** 创建 SSE Emitter */
    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(properties.getTimeout());

        // 异步异常 / 断开回调
        emitter.onCompletion(() -> removeEmitter(clientId));
        emitter.onTimeout(() -> removeEmitter(clientId));
        emitter.onError(ex -> {
            log.warn("SSE 客户端 {} 异常: {}", clientId, ex.getClass().getSimpleName());
            removeEmitter(clientId, false);
        });

        WrappedEmitter wrapped = new WrappedEmitter(emitter);
        WrappedEmitter old = emitters.put(clientId, wrapped);

        if (old != null) old.complete();

        connectionCount.incrementAndGet();
        listeners.forEach(listener -> safeOnConnect(listener, clientId));

        return emitter;
    }

    /** 发送消息给单个客户端 */
    public void sendEvent(String clientId, String eventName, Object data) {
        WrappedEmitter wrapped = emitters.get(clientId);
        if (wrapped == null || wrapped.isComplete()) return;

        executor.execute(() -> {
            try {
                wrapped.emitter.send(SseEmitter.event().name(eventName).data(data));
                totalMessagesSent.incrementAndGet();
                listeners.forEach(listener -> safeOnMessageSent(listener, clientId, eventName));
            } catch (IOException e) {
                log.warn("发送给客户端 {} 失败: {}", clientId, e.getMessage());
                failedMessages.incrementAndGet();
                removeEmitter(clientId, false);
            }
        });
    }

    /** 广播消息给所有客户端 */
    public void broadcast(String eventName, Object data) {
        executor.execute(() -> emitters.forEach((clientId, wrapped) -> {
            if (wrapped.isComplete()) {
                removeEmitter(clientId);
                return;
            }
            try {
                wrapped.emitter.send(SseEmitter.event().name(eventName).data(data));
                totalMessagesSent.incrementAndGet();
                listeners.forEach(listener -> safeOnMessageSent(listener, clientId, eventName));
            } catch (IOException e) {
                log.warn("广播客户端 {} 失败: {}", clientId, e.getMessage());
                failedMessages.incrementAndGet();
                removeEmitter(clientId, false);
            }
        }));
    }

    /** 移除客户端 */
    public void removeEmitter(String clientId) {
        removeEmitter(clientId, true);
    }

    public void removeEmitter(String clientId, boolean complete) {
        WrappedEmitter wrapped = emitters.remove(clientId);
        if (wrapped == null) return;

        connectionCount.decrementAndGet();

        if (complete) wrapped.complete();

        listeners.forEach(listener -> safeOnDisconnect(listener, clientId));
    }

    /** 安全调用 listener 方法 */
    private void safeOnConnect(SseEventListener listener, String clientId) {
        try {
            listener.onConnect(clientId);
        } catch (Exception e) {
            log.warn("Listener onConnect 异常: {}", clientId, e);
        }
    }

    private void safeOnMessageSent(SseEventListener listener, String clientId, String eventName) {
        try {
            listener.onMessageSent(clientId, eventName);
        } catch (Exception e) {
            log.warn("Listener onMessageSent 异常: {}", clientId, e);
        }
    }

    private void safeOnDisconnect(SseEventListener listener, String clientId) {
        try {
            listener.onDisconnect(clientId);
        } catch (Exception e) {
            log.warn("Listener onDisconnect 异常: {}", clientId, e);
        }
    }

    /** 启动心跳任务 */
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(
                () -> broadcast("heartbeat", "ping"),
                properties.getHeartbeatInterval(),
                properties.getHeartbeatInterval(),
                TimeUnit.MILLISECONDS);
    }

    /** 获取统计信息 */
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

    /** 获取活跃客户端 */
    public Set<String> getActiveClients() {
        return emitters.keySet();
    }

    /** 安全关闭 */
    public void shutdown() {
        scheduler.shutdown();
        executor.shutdown();
        emitters.values().forEach(WrappedEmitter::complete);
        emitters.clear();
    }

    /** 内部封装类 */
    private static class WrappedEmitter {
        final SseEmitter emitter;
        final AtomicBoolean completed = new AtomicBoolean(false);

        WrappedEmitter(SseEmitter emitter) {
            this.emitter = emitter;
        }

        boolean isComplete() {
            return completed.get();
        }

        void complete() {
            if (completed.compareAndSet(false, true)) {
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
