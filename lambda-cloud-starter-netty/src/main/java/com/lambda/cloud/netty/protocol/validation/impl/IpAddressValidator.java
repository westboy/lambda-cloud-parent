package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * IP地址验证器
 */
public class IpAddressValidator implements ProtocolValidator {
    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("IP地址不能为空");
        }

        if (!(value instanceof String ip)) {
            return ValidationResult.failure("IP地址必须为字符串类型");
        }

        if (ValidationUtils.isValidIp(ip)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("无效的IP地址格式: " + ip);
        }
    }
}
