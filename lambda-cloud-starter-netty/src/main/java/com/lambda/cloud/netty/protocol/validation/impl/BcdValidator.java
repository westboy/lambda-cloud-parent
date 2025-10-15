package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * BCD编码验证器
 */
public class BcdValidator implements ProtocolValidator {
    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("BCD编码不能为空");
        }

        if (!(value instanceof String bcd)) {
            return ValidationResult.failure("BCD编码必须为字符串类型");
        }

        if (ValidationUtils.isBcd(bcd)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("无效的BCD编码: " + bcd);
        }
    }
}
