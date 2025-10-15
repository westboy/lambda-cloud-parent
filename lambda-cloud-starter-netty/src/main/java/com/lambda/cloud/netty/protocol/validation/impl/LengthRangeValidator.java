package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * 长度范围验证器（可配置）
 */
public class LengthRangeValidator implements ProtocolValidator {
    private final int minLength;
    private final int maxLength;

    public LengthRangeValidator(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("字符串不能为空");
        }

        if (!(value instanceof String str)) {
            return ValidationResult.failure("必须为字符串类型");
        }

        if (ValidationUtils.isLengthInRange(str, minLength, maxLength)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(String.format("字符串长度必须在%d到%d之间: %d", minLength, maxLength, str.length()));
        }
    }
}
