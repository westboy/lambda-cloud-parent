package com.lambda.cloud.ykc.message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 统一响应生成器 + 多版本支持
 */
public class ResponseBuilder {

    private static final Map<String, Function<ProtocolMessage, ProtocolMessage>> BUILDER_MAP =
            new ConcurrentHashMap<>();

    private static String key(String frameType, String version) {
        return frameType + ":" + version;
    }

    public static void register(String frameType, String version, Function<ProtocolMessage, ProtocolMessage> builder) {
        BUILDER_MAP.put(key(frameType, version), builder);
    }

    @SuppressWarnings("unchecked")
    public static <Req extends ProtocolMessage, Res extends ProtocolMessage> Res buildResponse(
            Req request, String version) {
        Function<ProtocolMessage, ProtocolMessage> builder = BUILDER_MAP.get(key(request.getFrameType(), version));
        if (builder == null) {
            throw new IllegalStateException("No builder registered for " + key(request.getFrameType(), version));
        }
        return (Res) builder.apply(request);
    }
}
