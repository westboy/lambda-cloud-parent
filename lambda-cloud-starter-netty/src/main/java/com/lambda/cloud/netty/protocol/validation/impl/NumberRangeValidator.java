package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * 范围验证器（可配置）
 */
public record NumberRangeValidator(double min, double max) implements ProtocolValidator {

    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("数值不能为空");
        }

        if (!(value instanceof Number number)) {
            return ValidationResult.failure("必须为数值类型");
        }

        if (ValidationUtils.isInRange(number, min, max)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(String.format("数值必须在%.2f到%.2f之间: %.2f", min, max, number.doubleValue()));
        }
    }
}
