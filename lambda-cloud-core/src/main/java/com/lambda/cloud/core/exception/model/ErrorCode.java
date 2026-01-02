package com.lambda.cloud.core.exception.model;

import java.io.Serializable;

/**
 * 错误码接口
 * <p>
 * 定义了错误码的基本结构，包含错误码和错误信息。
 * 通常由枚举类实现，用于统一管理系统中的各种错误码。
 *
 * <p>该接口继承了 {@link Serializable}，支持序列化操作，
 * 便于在分布式系统中传递错误信息。
 *
 * <p>使用示例：
 * <pre>{@code
 * public enum CommonErrorCode implements ErrorCode {
 *     INVALID_PARAMETER("400", "参数无效"),
 *     UNAUTHORIZED("401", "未授权访问");
 *
 *     private final String code;
 *     private final String message;
 *
 *     // 构造方法和getter方法...
 * }
 * }</pre>
 *
 * @author Jin
 * @since 1.0.0
 */
public interface ErrorCode extends Serializable {

    /**
     * 获取错误码
     * <p>
     * 错误码通常是一个字符串，用于唯一标识特定的错误类型。
     * 建议使用有意义的编码规则，如HTTP状态码或自定义业务错误码。
     *
     * @return 错误码字符串，不应为null
     */
    Integer getCode();

    /**
     * 获取错误信息
     * <p>
     * 错误信息是对错误的详细描述，通常用于向用户展示或记录日志。
     * 应该提供清晰、准确的错误描述，便于问题定位和用户理解。
     *
     * @return 错误信息字符串，不应为null
     */
    String getMessage();
}
