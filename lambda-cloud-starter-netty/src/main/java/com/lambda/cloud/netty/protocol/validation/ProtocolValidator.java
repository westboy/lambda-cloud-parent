package com.lambda.cloud.netty.protocol.validation;

/**
 * 协议字段验证器接口
 * <p>
 * 用于自定义字段验证逻辑
 * </p>
 *
 */
public interface ProtocolValidator {

    /**
     * 验证字段值
     *
     * @param value 字段值
     * @return 验证结果
     */
    ValidationResult validate(Object value);


}
