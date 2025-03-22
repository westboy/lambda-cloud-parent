package com.lamuda.cloud.core.exception.feign;


import com.lamuda.cloud.core.exception.model.ErrorModel;
import lombok.Getter;

/**
 * @author jin
 */
public abstract class AbstractFeignException extends RuntimeException {

    @Getter
    private final long timestamp;

    @Getter
    private final String error;

    private final String message;

    @Getter
    private final String path;

    protected AbstractFeignException(ErrorModel model) {
        super(model.getMessage());
        this.path = model.getPath();
        this.error = model.getError();
        this.message = model.getMessage();
        this.timestamp = model.getTimestamp();
    }

    /**
     * 获取状态码
     * @return int 
     */
    public abstract int getStatus();

    @Override
    public String getMessage() {
        return message;
    }

}
