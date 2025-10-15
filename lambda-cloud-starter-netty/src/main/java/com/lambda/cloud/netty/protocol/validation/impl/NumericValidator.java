package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * 纯数字验证器
 */
public class NumericValidator implements ProtocolValidator {
    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("字符串不能为空");
        }

        if (!(value instanceof String str)) {
            return ValidationResult.failure("必须为字符串类型");
        }

        if (ValidationUtils.isNumeric(str)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("字符串只能包含数字: " + str);
        }
    }
}
