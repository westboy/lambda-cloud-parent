package com.lambda.cloud.netty.protocol.annotation;

/**
 * 协议字段验证器接口
 * <p>
 * 用于自定义字段验证逻辑
 * </p>
 *
 */
public interface ProtocolValidator {

    /**
     * 验证字段值
     *
     * @param value 字段值
     * @return 验证结果
     */
    ValidationResult validate(Object value);

    /**
     * 验证结果
     */
    record ValidationResult(boolean valid, String message) {
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }
}
