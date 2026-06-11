package com.lambda.cloud.core.exception.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ProblemDetail;

/**
 * 错误响应模型类
 * <p>
 * 用于封装API错误响应的标准格式，包含HTTP状态码、时间戳、错误信息、
 * 详细错误列表和请求路径等信息。
 *
 * <p>该类遵循RESTful API的错误响应规范，提供统一的错误信息结构，
 * 便于前端处理和错误日志记录。
 *
 * <p>错误列表使用不可变集合 {@link ImmutableList} 来确保数据安全性。
 *
 * @author jin
 * @since 1.0.0
 */
@Getter
@Setter
public class ErrorModel extends ProblemDetail {

    /**
     * 错误发生时间戳
     * <p>记录错误发生的具体时间，便于问题追踪
     */
    private long timestamp;

    /**
     * 错误类型描述
     * <p>简短的错误类型说明，如"Bad Request"、"Internal Server Error"等
     */
    private String error;

    /**
     * 错误详细信息
     * <p>对错误的详细描述，用于向用户展示或开发者调试
     */
    private String message;

    /**
     * 参数错误列表
     * <p>当请求参数验证失败时，包含具体的字段错误信息
     */
    private List<ArgumentError> errors;

    /**
     * 请求路径
     * <p>发生错误的API请求路径，便于定位问题
     */
    private String path;

    /**
     * 错误明细（扩展信息）
     * <p>
     * 用于携带与错误相关的补充上下文数据，如业务状态、约束条件、
     * 冲突资源信息或调试辅助信息等。
     * </p>
     */
    private Object details;

    /**
     * 错误代码
     * <p>
     * 用于表示具体的错误代码，通常与 {@link ErrorCode} 接口中的错误码相对应。
     * 该字段可以用于进一步细化错误类型，便于系统内部错误处理和日志记录。
     */
    private Integer code = 20000;

    /**
     * 设置参数错误列表
     * <p>
     * 使用不可变集合来确保错误列表的安全性，防止外部修改。
     * 如果传入null，则设置为空的不可变列表。
     *
     * @param errors 参数错误列表，可以为null
     */
    public void setErrors(List<ArgumentError> errors) {
        if (errors == null) {
            this.errors = ImmutableList.of();
        } else {
            this.errors = ImmutableList.copyOf(errors);
        }
    }
}
