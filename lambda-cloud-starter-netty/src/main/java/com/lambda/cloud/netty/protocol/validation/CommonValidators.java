package com.lambda.cloud.netty.protocol.validation;

import com.lambda.cloud.netty.protocol.annotation.ProtocolValidator;

/**
 * 常用验证器集合
 * <p>
 * 提供一些常用的验证器实现
 * </p>
 *
 */
public class CommonValidators {

    /**
     * IP地址验证器
     */
    public static class IpAddressValidator implements ProtocolValidator {
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

    /**
     * MAC地址验证器
     */
    public static class MacAddressValidator implements ProtocolValidator {
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

    /**
     * 十六进制字符串验证器
     */
    public static class HexStringValidator implements ProtocolValidator {
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

    /**
     * BCD编码验证器
     */
    public static class BcdValidator implements ProtocolValidator {
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

    /**
     * 正数验证器
     */
    public static class PositiveNumberValidator implements ProtocolValidator {
        @Override
        public ValidationResult validate(Object value) {
            if (value == null) {
                return ValidationResult.failure("数值不能为空");
            }

            if (!(value instanceof Number number)) {
                return ValidationResult.failure("必须为数值类型");
            }

            if (ValidationUtils.isPositive(number)) {
                return ValidationResult.success();
            } else {
                return ValidationResult.failure("数值必须为正数: " + number);
            }
        }
    }

    /**
     * 非负数验证器
     */
    public static class NonNegativeNumberValidator implements ProtocolValidator {
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

    /**
     * 时间戳验证器
     */
    public static class TimestampValidator implements ProtocolValidator {
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

    /**
     * 字母数字验证器
     */
    public static class AlphanumericValidator implements ProtocolValidator {
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

    /**
     * 纯数字验证器
     */
    public static class NumericValidator implements ProtocolValidator {
        @Override
        public ValidationResult validate(Object value) {
            if (value == null) {
                return ValidationResult.failure("字符串不能为空");
            }

            if (!(value instanceof String)) {
                return ValidationResult.failure("必须为字符串类型");
            }

            String str = (String) value;
            if (ValidationUtils.isNumeric(str)) {
                return ValidationResult.success();
            } else {
                return ValidationResult.failure("字符串只能包含数字: " + str);
            }
        }
    }

    /**
     * 范围验证器（可配置）
     */
    public static class RangeValidator implements ProtocolValidator {
        private final double min;
        private final double max;

        public RangeValidator(double min, double max) {
            this.min = min;
            this.max = max;
        }

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
                return ValidationResult.failure(
                        String.format("数值必须在%.2f到%.2f之间: %.2f", min, max, number.doubleValue()));
            }
        }
    }

    /**
     * 长度范围验证器（可配置）
     */
    public static class LengthRangeValidator implements ProtocolValidator {
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
                return ValidationResult.failure(
                        String.format("字符串长度必须在%d到%d之间: %d", minLength, maxLength, str.length()));
            }
        }
    }
}
