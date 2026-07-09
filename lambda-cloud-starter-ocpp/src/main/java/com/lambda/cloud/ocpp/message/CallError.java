package com.lambda.cloud.ocpp.message;

/**
 * OCPP-J CallError:{@code [4, MessageId, ErrorCode, ErrorDescription, ErrorDetails]}。对 Call 的错误应答。
 *
 * @param messageId        关联的 Call 消息 ID
 * @param errorCode        OCPP 错误码
 * @param errorDescription 人类可读错误描述
 * @param errorDetails     错误详情(可选,任意 JSON)
 */
public record CallError(String messageId, OcppErrorCode errorCode, String errorDescription, Object errorDetails)
        implements OcppMessage {
    @Override
    public String getMessageId() {
        return messageId;
    }
}
