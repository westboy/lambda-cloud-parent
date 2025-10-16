package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * MAC地址验证器
 */
public class MacAddressValidator implements ProtocolValidator {
    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("MAC地址不能为空");
        }

        if (!(value instanceof String mac)) {
            return ValidationResult.failure("MAC地址必须为字符串类型");
        }

        if (ValidationUtils.isValidMac(mac)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("无效的MAC地址格式: " + mac);
        }
    }
}
