package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * 时间戳验证器
 */
public class TimestampValidator implements ProtocolValidator {
    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("时间戳不能为空");
        }

        if (!(value instanceof Long timestamp)) {
            return ValidationResult.failure("时间戳必须为Long类型");
        }

        if (ValidationUtils.isValidTimestamp(timestamp)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("无效的时间戳: " + timestamp);
        }
    }
}
