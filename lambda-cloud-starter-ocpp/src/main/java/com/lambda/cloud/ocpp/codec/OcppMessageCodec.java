package com.lambda.cloud.ocpp.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.ocpp.action.OcppActionRegistry;
import com.lambda.cloud.ocpp.message.Call;
import com.lambda.cloud.ocpp.message.CallError;
import com.lambda.cloud.ocpp.message.CallResult;
import com.lambda.cloud.ocpp.message.MessageType;
import com.lambda.cloud.ocpp.message.OcppErrorCode;
import com.lambda.cloud.ocpp.message.OcppMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;

/**
 * OCPP-J JSON 编解码器(无状态、无业务依赖)。
 * <p>JSON 数组 {@code [TypeId, MessageId, Action?, Payload?]} <-> {@link OcppMessage}。</p>
 * <p>Call 的 payload 按 action 查 {@link OcppActionRegistry} 反序列化为对应请求类型;
 * CallResult 的 payload 反序列化为通用 {@code Object}(由调用方按上下文转型)。</p>
 * <p>消息关联(UniqueId -> pending Call)不在本类,属运行时状态。</p>
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class OcppMessageCodec {

    private final ObjectMapper objectMapper;
    private final OcppActionRegistry registry;

    public OcppMessageCodec(ObjectMapper objectMapper, OcppActionRegistry registry) {
        this.objectMapper = objectMapper;
        this.registry = registry;
    }

    /**
     * 解码 OCPP-J JSON 字符串为 {@link OcppMessage}。
     *
     * @param json OCPP-J JSON 数组字符串
     * @return Call/CallResult/CallError
     * @throws IOException JSON 解析失败
     */
    public OcppMessage decode(String json) throws IOException {
        JsonNode node = objectMapper.readTree(json);
        if (!node.isArray() || node.size() < 3) {
            throw new IllegalArgumentException("Invalid OCPP-J message, not a valid array: " + json);
        }
        int typeId = node.get(0).asInt();
        String messageId = node.get(1).asText();
        MessageType type = MessageType.fromCode(typeId);
        return switch (type) {
            case CALL -> {
                String action = node.get(2).asText();
                Class<?> reqClass = registry.requestClass(action);
                Object payload = reqClass != null
                        ? objectMapper.treeToValue(node.get(3), reqClass)
                        : objectMapper.treeToValue(node.get(3), Object.class);
                yield new Call(messageId, action, payload);
            }
            case CALL_RESULT -> {
                Object payload = objectMapper.treeToValue(node.get(2), Object.class);
                yield new CallResult(messageId, payload);
            }
            case CALL_ERROR -> {
                OcppErrorCode code = OcppErrorCode.fromWire(node.get(2).asText());
                String desc = node.get(3).asText();
                Object details = node.hasNonNull(4) ? objectMapper.treeToValue(node.get(4), Object.class) : null;
                yield new CallError(messageId, code, desc, details);
            }
        };
    }

    /**
     * 编码 {@link OcppMessage} 为 OCPP-J JSON 字符串。
     *
     * @param message Call/CallResult/CallError
     * @return OCPP-J JSON 数组字符串
     * @throws IOException JSON 序列化失败
     */
    public String encode(OcppMessage message) throws IOException {
        return switch (message) {
            case Call call -> {
                JsonNode payloadNode = objectMapper.valueToTree(call.payload());
                yield objectMapper.writeValueAsString(
                        new Object[] {MessageType.CALL.getCode(), call.messageId(), call.action(), payloadNode});
            }
            case CallResult result -> {
                JsonNode payloadNode = objectMapper.valueToTree(result.payload());
                yield objectMapper.writeValueAsString(
                        new Object[] {MessageType.CALL_RESULT.getCode(), result.messageId(), payloadNode});
            }
            case CallError error ->
                objectMapper.writeValueAsString(new Object[] {
                    MessageType.CALL_ERROR.getCode(),
                    error.messageId(),
                    error.errorCode().wire(),
                    error.errorDescription(),
                    error.errorDetails()
                });
        };
    }
}
