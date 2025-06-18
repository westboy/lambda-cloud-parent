package com.lambda.cloud.sse.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultSseEventListener
 *
 * @author Jin
 */
public class DefaultSseEventListener implements SseEventListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSseEventListener.class);

    @Override
    public void onConnect(String clientId) {
        logger.info("SSE Client connected: {}", clientId);
    }

    @Override
    public void onDisconnect(String clientId) {
        logger.info("SSE Client disconnected: {}", clientId);
    }

    @Override
    public void onMessageSent(String clientId, String eventName) {
        logger.debug("SSE Message sent to client {}: {}", clientId, eventName);
    }
}
