package com.lambda.cloud.mvc.execption;

import cn.hutool.core.util.ArrayUtil;
import com.lambda.cloud.core.exception.model.ErrorCode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * @author Jin
 */
@Getter
@SuppressWarnings("all")
@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class BusinessException extends RuntimeException {

    private final transient String code;

    private final transient Object[] args;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        this(errorCode.getCode(), errorCode.getMessage(), args);
    }

    public BusinessException(String code, String msg) {
        super(msg);
        this.code = code;
        this.args = null;
    }

    public BusinessException(String code, String msg, Object... args) {
        super(msg);
        this.code = code;
        this.args = ArrayUtil.clone(args);
    }

    public BusinessException(String code, String msg, Object[] args, Throwable t) {
        super(msg, t);
        this.code = code;
        this.args = args;
    }
}
