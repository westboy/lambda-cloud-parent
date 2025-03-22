package com.lamuda.cloud.core.exception.model;

/**
 * @author Jin
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return java.lang.String
     */
    String getCode();

    /**
     * 获取错误信息
     *
     * @return java.lang.String
     */
    String getMessage();
}
