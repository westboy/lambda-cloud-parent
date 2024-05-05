package com.jingfang.cloud.mvc.execption;

import lombok.Getter;

/**
 * @author Jin
 */
@Getter
public class BusinessException extends RuntimeException {

    private final transient int code;

    private final transient Object[] args;

    public BusinessException(BusinessErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(BusinessErrorCode errorCode, Object... args) {
        this(errorCode.getCode(), errorCode.getMessage(), args);
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.args = null;
    }

    public BusinessException(int code, String msg, Object... args) {
        super(msg);
        this.code = code;
        this.args = args;
    }

    public BusinessException(int code, String msg, Object[] args, Throwable t) {
        super(msg, t);
        this.code = code;
        this.args = args;
    }

}
