package com.lamuda.cloud.mvc.execption;

/**
 * @author Jin
 */
public interface BusinessErrorCode {
    /**
     * 获取错误码
     * @return int code
     */
    int getCode();

    /**
     * 获取错误信息
     * @return String message
     */
    String getMessage();
}
