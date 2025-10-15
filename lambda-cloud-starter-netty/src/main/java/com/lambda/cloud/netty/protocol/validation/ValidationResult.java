package com.lambda.cloud.netty.protocol.validation;

/**
 * 验证结果
 */
public record ValidationResult(boolean valid, String message) {
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }
}