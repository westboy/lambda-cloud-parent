package com.lambda.cloud.oss.util;

import java.io.InputStream;

/**
 * 参数校验工具类
 * 
 * @author jpjoo
 */
public final class ValidationUtils {

    /**
     * 校验对象不能为 null
     * 
     * @param value 待校验的值
     * @param paramName 参数名称
     * @throws IllegalArgumentException 如果值为 null
     */
    public static void validateNotNull(Object value, String paramName) {
        if (value == null) {
            throw new IllegalArgumentException(paramName + " 不能为 null");
        }
    }

    /**
     * 校验字符串不能为空
     * 
     * @param value 待校验的字符串
     * @param paramName 参数名称
     * @throws IllegalArgumentException 如果字符串为 null 或空
     */
    public static void validateNotBlank(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " 不能为空");
        }
    }

    /**
     * 校验对象键（objectKey）
     * 
     * @param objectKey 对象键
     * @throws IllegalArgumentException 如果对象键无效
     */
    public static void validateObjectKey(String objectKey) {
        validateNotBlank(objectKey, "objectKey");
        
        if (objectKey.length() > 1024) {
            throw new IllegalArgumentException("objectKey 长度不能超过 1024 个字符");
        }
        
        // 检查是否包含非法字符
        if (objectKey.contains("//")) {
            throw new IllegalArgumentException("objectKey 不能包含连续的斜杠");
        }
        
        // 不能以斜杠开头
        if (objectKey.startsWith("/")) {
            throw new IllegalArgumentException("objectKey 不能以斜杠开头");
        }
    }

    /**
     * 校验内容类型
     * 
     * @param contentType 内容类型
     * @throws IllegalArgumentException 如果内容类型无效
     */
    public static void validateContentType(String contentType) {
        validateNotBlank(contentType, "contentType");
    }

    /**
     * 校验输入流
     * 
     * @param inputStream 输入流
     * @throws IllegalArgumentException 如果输入流为 null
     */
    public static void validateInputStream(InputStream inputStream) {
        validateNotNull(inputStream, "inputStream");
    }

    /**
     * 校验分片参数
     * 
     * @param partNumber 当前分片号
     * @param partTotalNumber 总分片数
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void validatePartNumbers(int partNumber, int partTotalNumber) {
        if (partNumber < 1) {
            throw new IllegalArgumentException("partNumber 必须大于 0");
        }
        if (partTotalNumber < 1) {
            throw new IllegalArgumentException("partTotalNumber 必须大于 0");
        }
        if (partNumber > partTotalNumber) {
            throw new IllegalArgumentException("partNumber 不能大于 partTotalNumber");
        }
    }

    /**
     * 校验过期时间（秒）
     * 
     * @param seconds 过期时间（秒）
     * @throws IllegalArgumentException 如果时间无效
     */
    public static void validateExpirationSeconds(Integer seconds) {
        validateNotNull(seconds, "expirationSeconds");
        if (seconds < 1) {
            throw new IllegalArgumentException("expirationSeconds 必须大于 0");
        }
        if (seconds > 604800) { // 7 天
            throw new IllegalArgumentException("expirationSeconds 不能超过 604800 秒（7 天）");
        }
    }
}
