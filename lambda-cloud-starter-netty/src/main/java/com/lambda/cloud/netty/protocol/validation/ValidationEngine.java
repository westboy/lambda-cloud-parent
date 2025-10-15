package com.lambda.cloud.netty.protocol.validation;

import com.lambda.cloud.netty.protocol.annotation.ProtocolValidation;
import com.lambda.cloud.netty.protocol.annotation.ProtocolValidator;
import com.lambda.cloud.netty.protocol.core.FieldMetadata;
import com.lambda.cloud.netty.protocol.core.MessageMetadata;
import com.lambda.cloud.netty.protocol.ProtocolEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * 验证引擎
 * <p>
 * 提供统一的消息验证功能，支持性能优化的缓存机制
 * </p>
 *
 * @author Jin
 */
@Slf4j
public class ValidationEngine {

    /**
     * 正则表达式Pattern缓存，避免重复编译
     */
    private final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<>();

    /**
     * 验证器实例缓存，避免重复创建
     */
    private final ConcurrentHashMap<Class<? extends ProtocolValidator>, ProtocolValidator> validatorCache =
            new ConcurrentHashMap<>();

    /**
     * 验证消息
     *
     * @param message  消息对象
     * @param metadata 消息元数据
     * @return 验证结果
     */
    public ProtocolEngine.ValidationResult validate(Object message, MessageMetadata metadata) {
        List<String> errors = new ArrayList<>();

        try {
            for (FieldMetadata fieldMetadata : metadata.fields()) {
                if (fieldMetadata.hasValidation()) {
                    validateField(message, fieldMetadata, errors);
                }
            }

            if (errors.isEmpty()) {
                return ProtocolEngine.ValidationResult.success();
            } else {
                return ProtocolEngine.ValidationResult.failure(String.join("; ", errors));
            }
        } catch (Exception e) {
            log.error("验证过程中发生异常", e);
            return ProtocolEngine.ValidationResult.failure("验证过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 验证字段
     *
     * @param message       消息对象
     * @param fieldMetadata 字段元数据
     * @param errors        错误列表
     */
    private void validateField(Object message, FieldMetadata fieldMetadata, List<String> errors) {
        try {
            Object value = fieldMetadata.getValue(message);
            ProtocolValidation validation = fieldMetadata.validation();

            // 空值检查
            if (value == null) {
                if (!validation.nullable()) {
                    errors.add("字段 " + fieldMetadata.getFieldName() + " 不能为空");
                }
                return; // 空值不进行其他验证
            }

            // 数值范围验证
            if (value instanceof Number) {
                validateNumberRange((Number) value, validation, fieldMetadata.getFieldName(), errors);
            }

            // 字符串长度验证
            if (value instanceof String) {
                validateStringLength((String) value, validation, fieldMetadata.getFieldName(), errors);
            }

            // 正则表达式验证
            if (!validation.pattern().isEmpty()) {
                validateRegex(value.toString(), validation.pattern(), fieldMetadata.getFieldName(), errors);
            }

            // 自定义验证器
            if (validation.validator() != ProtocolValidator.class) {
                validateCustom(value, validation.validator(), fieldMetadata.getFieldName(), errors);
            }

        } catch (Exception e) {
            log.error("验证字段 {} 时发生异常", fieldMetadata.getFieldName(), e);
            errors.add("验证字段 " + fieldMetadata.getFieldName() + " 时发生异常: " + e.getMessage());
        }
    }

    /**
     * 验证数值范围
     *
     * @param value     数值
     * @param validation 验证注解
     * @param fieldName 字段名
     * @param errors    错误列表
     */
    private void validateNumberRange(
            Number value, ProtocolValidation validation, String fieldName, List<String> errors) {
        double doubleValue = value.doubleValue();

        if (validation.min() != Double.MIN_VALUE && doubleValue < validation.min()) {
            errors.add("字段 " + fieldName + " 的值 " + doubleValue + " 小于最小值 " + validation.min());
        }

        if (doubleValue > validation.max()) {
            errors.add("字段 " + fieldName + " 的值 " + doubleValue + " 大于最大值 " + validation.max());
        }
    }

    /**
     * 验证字符串长度
     *
     * @param value     字符串值
     * @param validation 验证注解
     * @param fieldName 字段名
     * @param errors    错误列表
     */
    private void validateStringLength(
            String value, ProtocolValidation validation, String fieldName, List<String> errors) {
        int length = value.length();

        if (validation.minLength() > 0 && length < validation.minLength()) {
            errors.add("字段 " + fieldName + " 的长度 " + length + " 小于最小长度 " + validation.minLength());
        }

        if (validation.maxLength() > 0 && length > validation.maxLength()) {
            errors.add("字段 " + fieldName + " 的长度 " + length + " 大于最大长度 " + validation.maxLength());
        }
    }

    /**
     * 验证正则表达式
     *
     * @param value     字符串值
     * @param regex     正则表达式
     * @param fieldName 字段名
     * @param errors    错误列表
     */
    private void validateRegex(String value, String regex, String fieldName, List<String> errors) {
        try {
            // 使用缓存的Pattern对象，避免重复编译
            Pattern pattern = patternCache.computeIfAbsent(regex, Pattern::compile);
            if (!pattern.matcher(value).matches()) {
                errors.add("字段 " + fieldName + " 的值 \"" + value + "\" 不匹配正则表达式 \"" + regex + "\"");
            }
        } catch (Exception e) {
            log.error("正则表达式验证失败: {}", regex, e);
            errors.add("字段 " + fieldName + " 的正则表达式验证失败: " + e.getMessage());
        }
    }

    /**
     * 自定义验证器验证
     *
     * @param value         字段值
     * @param validatorClass 验证器类
     * @param fieldName     字段名
     * @param errors        错误列表
     */
    private void validateCustom(
            Object value, Class<? extends ProtocolValidator> validatorClass, String fieldName, List<String> errors) {
        try {
            // 使用缓存的验证器实例，避免重复创建
            ProtocolValidator validator = validatorCache.computeIfAbsent(validatorClass, clazz -> {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    log.error("创建验证器实例失败: {}", clazz.getName(), e);
                    throw new RuntimeException("创建验证器实例失败", e);
                }
            });
            ProtocolValidator.ValidationResult validate = validator.validate(value);
            if (!validate.valid()) {
                errors.add("字段 " + fieldName + " 未通过自定义验证器 " + validatorClass.getSimpleName() + " 的验证");
            }
        } catch (Exception e) {
            log.error("自定义验证器执行失败: {}", validatorClass.getName(), e);
            errors.add("字段 " + fieldName + " 的自定义验证器执行失败: " + e.getMessage());
        }
    }
}
