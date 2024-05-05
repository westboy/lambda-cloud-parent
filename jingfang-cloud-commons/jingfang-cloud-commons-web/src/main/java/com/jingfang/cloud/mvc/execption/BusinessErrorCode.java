package com.jingfang.cloud.mvc.execption;

/**
 * @author Jin
 */
public interface BusinessErrorCode {
    /**
     * 获取错误码
     */
    int getCode();

    /**
     * 获取错误信息
     */
    String getMessage();
}
