package com.lambda.cloud.netty.protocol.exception;

import lombok.Getter;

/**
 * 协议处理异常
 * <p>
 * 协议解析、序列化和验证过程中的统一异常类型
 * </p>
 *
 * @author Jin
 */
@Getter
public class ProtocolException extends Exception {

    private final ErrorCode errorCode;
    private final String fieldName;

    public ProtocolException(String message) {
        this(ErrorCode.UNKNOWN, message, null, null);
    }

    public ProtocolException(String message, Throwable cause) {
        this(ErrorCode.UNKNOWN, message, null, cause);
    }

    public ProtocolException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public ProtocolException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public ProtocolException(ErrorCode errorCode, String message, String fieldName) {
        this(errorCode, message, fieldName, null);
    }

    public ProtocolException(ErrorCode errorCode, String message, String fieldName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.fieldName = fieldName;
    }

    /**
     * 错误代码枚举
     */
    @Getter
    public enum ErrorCode {
        UNKNOWN("未知错误"),
        PARSE_ERROR("解析错误"),
        SERIALIZE_ERROR("序列化错误"),
        VALIDATION_ERROR("验证错误"),
        FIELD_NOT_FOUND("字段未找到"),
        TYPE_MISMATCH("类型不匹配"),
        LENGTH_MISMATCH("长度不匹配"),
        BUFFER_UNDERFLOW("缓冲区数据不足"),
        ANNOTATION_MISSING("注解缺失"),
        REFLECTION_ERROR("反射错误"),
        CRC_ERROR("CRC处理错误"),
        CRC_VALIDATION_ERROR("CRC校验失败");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }
    }
}
