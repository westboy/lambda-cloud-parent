package com.lambda.cloud.t645.message;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * T645 协议载荷注册表。
 *
 * <p>以「控制码:DI」为键，注册并查找对应的 {@link T645DataBody} 实现类，
 * 用于报文解码时根据控制码和数据标识自动路由到正确的业务载荷类。</p>
 *
 * <p>键格式：{@code 控制码(2位HEX):DI(8位HEX)}，例如 {@code 11:00010000}。</p>
 *
 * @see com.lambda.autoconfig.T645AutoConfiguration#registerBuiltinPayloads()
 */
@Slf4j
public final class T645PayloadRegistry {

    /** 载荷注册表，键为 "控制码:DI"，值为对应的 T645DataBody 实现类。 */
    private static final Map<String, Class<?>> REGISTRY = new ConcurrentHashMap<>();

    private T645PayloadRegistry() {}

    /**
     * 注册载荷映射。
     *
     * @param controlCode 控制码（如 0x11=读数据，0x91=读数据应答）
     * @param di 数据标识（4 字节十六进制字符串，如 "00010000"）；无 DI 时传 null 或空
     * @param bodyClass 对应的 {@link T645DataBody} 实现类
     */
    public static void register(int controlCode, String di, Class<?> bodyClass) {
        String key = buildKey(controlCode, di);
        REGISTRY.put(key, bodyClass);
        log.info("Registered T645 payload [{}] -> {}", key, bodyClass.getSimpleName());
    }

    /**
     * 根据控制码和数据标识查找已注册的载荷类。
     *
     * @param controlCode 控制码
     * @param di 数据标识，可为 null
     * @return 对应的载荷类，未找到时返回 null
     */
    public static Class<?> lookup(int controlCode, String di) {
        return REGISTRY.get(buildKey(controlCode, di));
    }

    /**
     * 判断控制码是否属于 DL/T 645-2007 标准报文。
     *
     * <p>标准报文的数据域使用 +0x33 偏移编码；而 4G/NB 私有心跳报文
     * （控制码 0x00/0x80）数据域保留原始字节，不做偏移处理。</p>
     *
     * @param controlCode 控制码
     * @return {@code true} 表示标准报文，{@code false} 表示私有报文
     */
    public static boolean isStandardControlCode(int controlCode) {
        // 心跳上报(0x00)和应答(0x80)属于私有报文，数据域保留原始字节
        return controlCode != 0x00 && controlCode != 0x80;
    }

    /**
     * 获取所有已注册的载荷映射（副本）。
     *
     * @return 键值对副本，键为 "控制码:DI"，值为载荷类
     */
    public static Map<String, Class<?>> getAll() {
        return new ConcurrentHashMap<>(REGISTRY);
    }

    /** 清空所有已注册的载荷映射（主要用于测试）。 */
    public static void clear() {
        REGISTRY.clear();
    }

    /**
     * 构建注册表查找键。
     *
     * @param controlCode 控制码
     * @param di 数据标识，为 null 或空时使用 "NONE"
     * @return 格式为 {@code XX:YYYYYYYY} 的键字符串
     */
    private static String buildKey(int controlCode, String di) {
        if (di == null || di.isEmpty()) {
            return String.format("%02X:NONE", controlCode & 0xFF);
        }
        return String.format("%02X:%s", controlCode & 0xFF, di.toUpperCase(Locale.ROOT));
    }
}
