package com.lambda.cloud.netty.protocol.message;

import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtocolPayloadRegistry {
    private static final Map<String, Class<?>> protocolMessageStore = new ConcurrentHashMap<>();

    public static void register(String protocol, Class<?> protocolMessage) {
        if (protocol == null || protocolMessage == null) {
            log.warn("Protocol or protocolMessage is null, skipping registration");
            return;
        }

        ProtocolPayload annotation = protocolMessage.getAnnotation(ProtocolPayload.class);
        if (annotation == null) {
            log.warn(
                    "Class {} does not have @ProtocolPayload annotation, skipping registration",
                    protocolMessage.getName());
            return;
        }

        if (annotation.isFrame()) {
            log.debug("Class {} has isFrame=true, skipping registration", protocolMessage.getName());
            return;
        }

        String normalizedProtocol = normalizeProtocol(protocol);
        protocolMessageStore.put(normalizedProtocol, protocolMessage);
        log.info(
                "Registered protocol [{}] (normalized: {}) with class: {}",
                protocol,
                normalizedProtocol,
                protocolMessage.getName());
    }

    public static Class<?> getProtocolMessage(String protocol) {
        if (protocol == null) {
            return null;
        }
        String normalizedProtocol = normalizeProtocol(protocol);
        return protocolMessageStore.get(normalizedProtocol);
    }

    private static String normalizeProtocol(String protocol) {
        if (protocol == null || protocol.isEmpty()) {
            return protocol;
        }

        if (protocol.toLowerCase().startsWith("0x")) {
            try {
                int value = Integer.parseInt(protocol.substring(2), 16);
                return String.format("%02X", value);
            } catch (NumberFormatException e) {
                log.warn("Invalid hex format: {}, using original value", protocol);
                return protocol.substring(2).toUpperCase();
            }
        }

        try {
            int value = Integer.parseInt(protocol, 16);
            return String.format("%02X", value);
        } catch (NumberFormatException e) {
            return protocol.toUpperCase();
        }
    }

    public static Map<String, Class<?>> getAllProtocols() {
        return new ConcurrentHashMap<>(protocolMessageStore);
    }

    public static void clear() {
        protocolMessageStore.clear();
        log.info("Protocol registry cleared");
    }

    public static boolean unregister(String protocol) {
        if (protocol == null) {
            return false;
        }
        String normalizedProtocol = normalizeProtocol(protocol);
        Class<?> removed = protocolMessageStore.remove(normalizedProtocol);
        if (removed != null) {
            log.info("Unregistered protocol [{}] (normalized: {})", protocol, normalizedProtocol);
            return true;
        }
        return false;
    }
}
