package com.lambda.cloud.core.exception.model;

import java.io.Serializable;

/**
 * @author Jin
 */
public interface ErrorCode extends Serializable {

    /**
     * 获取错误码
     *
     * @return java.lang. Integer
     */
    Integer getCode();

    /**
     * 获取错误信息
     *
     * @return java.lang.String
     */
    String getMessage();
}
