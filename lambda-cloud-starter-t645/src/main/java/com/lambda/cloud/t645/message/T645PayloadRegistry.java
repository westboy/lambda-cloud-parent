package com.lambda.cloud.t645.message;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class T645PayloadRegistry {

    private static final Map<String, Class<?>> REGISTRY = new ConcurrentHashMap<>();

    private T645PayloadRegistry() {}

    public static void register(int controlCode, String di, Class<?> bodyClass) {
        String key = buildKey(controlCode, di);
        REGISTRY.put(key, bodyClass);
        log.info("Registered T645 payload [{}] -> {}", key, bodyClass.getSimpleName());
    }

    public static Class<?> lookup(int controlCode, String di) {
        return REGISTRY.get(buildKey(controlCode, di));
    }

    public static Map<String, Class<?>> getAll() {
        return new ConcurrentHashMap<>(REGISTRY);
    }

    public static void clear() {
        REGISTRY.clear();
    }

    private static String buildKey(int controlCode, String di) {
        return String.format("%02X:%s", controlCode & 0xFF, di.toUpperCase(Locale.ROOT));
    }
}
