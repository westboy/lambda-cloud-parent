package com.lambda.cloud.ocpp.message;

/**
 * OCPP-J 消息类型标识(Call/CallResult/CallError)。
 * <p>对应 JSON 数组首元素:2=Call,3=CallResult,4=CallError。</p>
 */
public enum MessageType {
    CALL(2),
    CALL_RESULT(3),
    CALL_ERROR(4);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MessageType fromCode(int code) {
        return switch (code) {
            case 2 -> CALL;
            case 3 -> CALL_RESULT;
            case 4 -> CALL_ERROR;
            default -> throw new IllegalArgumentException("Unknown OCPP message type code: " + code);
        };
    }
}
