package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * 字母数字验证器
 */
public class AlphanumericValidator implements ProtocolValidator {
    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("字符串不能为空");
        }

        if (!(value instanceof String str)) {
            return ValidationResult.failure("必须为字符串类型");
        }

        if (ValidationUtils.isAlphanumeric(str)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("字符串只能包含字母和数字: " + str);
        }
    }
}
