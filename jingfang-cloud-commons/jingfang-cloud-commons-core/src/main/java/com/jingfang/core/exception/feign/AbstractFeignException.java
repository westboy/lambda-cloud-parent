package com.jingfang.core.exception.feign;


import com.jingfang.core.exception.model.ErrorModel;

/**
 * @author jin
 */
public abstract class AbstractFeignException extends RuntimeException {

    private final long timestamp;

    private final String error;

    private final String message;

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
     */
    public abstract int getStatus();

    public long getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

}
