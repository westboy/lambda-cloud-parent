package com.lambda.cloud.ocpp.message;

/**
 * OCPP-J 消息信封基类。具体为 {@link Call}/{@link CallResult}/{@link CallError}。
 * <p>每条消息以 {@code messageId} 关联 Call 与其应答(CallResult/CallError)。</p>
 */
public sealed interface OcppMessage permits Call, CallResult, CallError {

    String getMessageId();
}
