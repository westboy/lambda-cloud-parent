package com.lambda.cloud.ocpp.message;

/**
 * OCPP-J Call:{@code [2, MessageId, Action, Payload]}。CSMS 与 CP 之间的双向请求。
 *
 * @param messageId 消息唯一标识,用于关联应答
 * @param action    OCPP Action,如 "Authorize"
 * @param payload   请求载荷对象(具体类型由 action 决定,经 {@code OcppActionRegistry} 反序列化)
 */
public record Call(String messageId, String action, Object payload) implements OcppMessage {
    @Override
    public String getMessageId() {
        return messageId;
    }
}
