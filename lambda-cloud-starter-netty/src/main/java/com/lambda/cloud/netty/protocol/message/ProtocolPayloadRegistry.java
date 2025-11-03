package com.lambda.cloud.netty.protocol.message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolPayloadRegistry {

    private static final Map<String, Class<?>> protocolMessageStore = new ConcurrentHashMap<>();

    public static void register(String protocol, Class<?> protocolMessage) {
        protocolMessageStore.put(protocol, protocolMessage);
    }

    public static Class<?> getProtocolMessage(String protocol) {
        return protocolMessageStore.get(protocol);
    }
}
