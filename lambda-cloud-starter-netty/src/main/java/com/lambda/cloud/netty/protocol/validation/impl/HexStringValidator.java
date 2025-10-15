package com.lambda.cloud.netty.protocol.validation.impl;

import com.lambda.cloud.netty.protocol.validation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.ValidationUtils;

/**
 * 十六进制字符串验证器
 */
public class HexStringValidator implements ProtocolValidator {
    @Override
    public ValidationResult validate(Object value) {
        if (value == null) {
            return ValidationResult.failure("十六进制字符串不能为空");
        }

        if (!(value instanceof String hex)) {
            return ValidationResult.failure("十六进制字符串必须为字符串类型");
        }

        if (ValidationUtils.isHex(hex)) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("无效的十六进制字符串: " + hex);
        }
    }
}
