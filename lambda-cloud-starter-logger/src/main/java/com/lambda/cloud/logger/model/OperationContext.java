package com.lambda.cloud.logger.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 操作上下文模型类。
 * <p>
 * 【命名说明】原类名：OperationDetail -> 新类名：OperationContext
 * <p>
 * 该类用于封装操作日志的详细信息，包括请求参数、请求体、
 * 响应结果等具体的操作执行细节。主要用于详细的操作追踪和问题排查。
 * <p>
 * 模型包含的详细信息：
 * <ul>
 *     <li>操作标识：操作ID、请求URI</li>
 *     <li>请求信息：请求参数、请求消息体</li>
 *     <li>响应信息：执行结果</li>
 * </ul>
 * <p>
 * 该类通常作为 {@link OperationLogRecord#getDetail()} 的 JSON 内容，
 * 提供操作的完整上下文信息。
 *
 * @author jpjoo
 * @since 1.0.0
 * @see OperationLogRecord
 */
@Getter
@Setter
@ToString
@Schema(description = "操作上下文模型")
public class OperationContext {

    /**
     * 操作的唯一标识。
     * <p>
     * 用于标识具体的操作类型或操作名称，便于操作的分类和统计。
     * 通常与 {@link com.lambda.cloud.logger.annotation.OperationLog#value()} 对应。
     */
    @Schema(description = "操作的唯一标识", example = "user_login")
    private String operationId;

    /**
     * 请求的 URI 地址。
     * <p>
     * 记录触发操作的具体请求路径，用于定位操作的入口点。
     * 包含完整的请求路径，但不包含查询参数。
     */
    @Schema(description = "请求的 URI 地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "/api/user/login")
    private String uri;

    /**
     * 请求参数集合。
     * <p>
     * 包含 HTTP 请求中的查询参数（Query Parameters），
     * 以键值对的形式存储，支持一个参数名对应多个值的情况。
     * 通常来源于 {@code HttpServletRequest.getParameterMap()}。
     */
    @Schema(description = "请求参数集合（Query Parameters）", example = "{\"page\": [\"1\"], \"size\": [\"10\"]}")
    private Object parameters;

    /**
     * 请求消息体内容。
     * <p>
     * 包含 HTTP 请求的消息体内容，通常是 POST、PUT 等请求中的 JSON 数据。
     * 仅当方法参数中包含 {@code @RequestBody} 注解时才会记录。
     */
    @Schema(description = "请求消息体内容（Request Body）", example = "{\"username\": \"admin\", \"password\": \"******\"}")
    private Object body;

    /**
     * 操作执行结果。
     * <p>
     * 记录方法执行的返回值或异常信息：
     * <ul>
     *     <li>正常执行：记录方法的返回值</li>
     *     <li>异常执行：记录异常的堆栈信息</li>
     * </ul>
     * 用于操作结果的追踪和问题排查。
     */
    @Schema(description = "操作执行结果或异常信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private Object result;
}
