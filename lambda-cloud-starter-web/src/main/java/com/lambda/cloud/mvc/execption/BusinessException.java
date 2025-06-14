package com.lambda.cloud.mvc.execption;

import cn.hutool.core.util.ArrayUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * @author Jin
 */
@Getter
@SuppressWarnings("all")
@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
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
        this.args = ArrayUtil.clone(args);
    }

    public BusinessException(int code, String msg, Object[] args, Throwable t) {
        super(msg, t);
        this.code = code;
        this.args = args;
    }
}
