package com.lambda.cloud.ocpp.action;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OCPP action -> 请求/响应类注册表。
 * <p>由 {@code OcppAutoConfiguration} 注册内置 action;运行时可追加自定义 action。</p>
 * <p>编解码器据此将 Call 的 payload 反序列化为对应请求类型。</p>
 */
public final class OcppActionRegistry {

    private final Map<String, Class<?>> requestClasses = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> responseClasses = new ConcurrentHashMap<>();

    /**
     * 注册一个 action 的请求/响应类型。
     *
     * @param action         OCPP Action 名(见 {@link OcppAction})
     * @param requestClass   请求载荷类型
     * @param responseClass  应答载荷类型
     */
    public void register(String action, Class<?> requestClass, Class<?> responseClass) {
        requestClasses.put(action, requestClass);
        responseClasses.put(action, responseClass);
    }

    /** 返回 action 对应的请求类型;未注册返回 null。 */
    public Class<?> requestClass(String action) {
        return requestClasses.get(action);
    }

    /** 返回 action 对应的应答类型;未注册返回 null。 */
    public Class<?> responseClass(String action) {
        return responseClasses.get(action);
    }

    /** 是否已注册该 action。 */
    public boolean contains(String action) {
        return requestClasses.containsKey(action);
    }
}
