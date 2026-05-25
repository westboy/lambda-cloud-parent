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

    public static boolean isStandardControlCode(int controlCode) {
        // 心跳上报(0x00)和应答(0x80)属于私有报文，数据域保留原始字节
        return controlCode != 0x00 && controlCode != 0x80;
    }

    public static Map<String, Class<?>> getAll() {
        return new ConcurrentHashMap<>(REGISTRY);
    }

    public static void clear() {
        REGISTRY.clear();
    }

    private static String buildKey(int controlCode, String di) {
        if (di == null || di.isEmpty()) {
            return String.format("%02X:NONE", controlCode & 0xFF);
        }
        return String.format("%02X:%s", controlCode & 0xFF, di.toUpperCase(Locale.ROOT));
    }
}
