package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * 非负数验证器
 */
public class NonNegativeNumberValidator implements ProtocolValidator {
    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("数值不能为空");
        }

        if (!(value instanceof Number number)) {
            return ValidationResult.failure("必须为数值类型");
        }

        if (ValidationUtils.isNonNegative(number)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("数值不能为负数: " + number);
        }
    }
}
