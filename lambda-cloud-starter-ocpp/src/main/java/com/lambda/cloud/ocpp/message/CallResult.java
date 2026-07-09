package com.lambda.cloud.ocpp.message;

/**
 * OCPP-J CallResult:{@code [3, MessageId, Payload]}。对 Call 的成功应答。
 *
 * @param messageId 关联的 Call 消息 ID
 * @param payload   应答载荷对象
 */
public record CallResult(String messageId, Object payload) implements OcppMessage {
    @Override
    public String getMessageId() {
        return messageId;
    }
}
