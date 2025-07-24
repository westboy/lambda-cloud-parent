package com.lambda.cloud.logger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 操作日志记录模型类。
 * <p>
 * 【命名说明】原类名：OperationBody -> 新类名：OperationLogRecord
 * <p>
 * 该类用于封装操作日志的核心信息，包括操作者信息、操作内容、
 * 执行时间、耗时等关键数据。主要用于日志记录、审计追踪等场景。
 * <p>
 * 模型包含的主要信息：
 * <ul>
 *     <li>操作标识：ID、方法名、模块名</li>
 *     <li>操作内容：描述、详情、HTTP方法类型</li>
 *     <li>操作者信息：操作人员、操作人员ID、租户ID</li>
 *     <li>执行信息：操作时间、执行耗时、客户端IP</li>
 * </ul>
 *
 * @author jpjoo
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Schema(description = "操作日志记录模型")
@SuppressFBWarnings("EI_EXPOSE_REP")
public class OperationLogRecord {

    /**
     * 日志记录的唯一标识。
     * <p>
     * 用于唯一标识一条操作日志记录，便于后续的查询和关联。
     */
    @Schema(description = "日志记录唯一标识", example = "log_20231201_001")
    private String id;

    /**
     * 执行的方法名称。
     * <p>
     * 记录触发日志记录的具体方法，通常为类名.方法名的格式。
     */
    @Schema(description = "执行的方法名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "UserController.login")
    private String method;

    /**
     * 操作所属的业务模块。
     * <p>
     * 用于对操作进行模块化分类，便于日志的分类管理和统计分析。
     */
    @Schema(description = "操作所属的业务模块", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户管理")
    private String module;

    /**
     * 操作的详细描述。
     * <p>
     * 对本次操作的详细说明，帮助理解操作的具体内容和目的。
     */
    @Schema(description = "操作的详细描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户登录操作")
    private String description;

    /**
     * HTTP 请求方法类型。
     * <p>
     * 记录触发操作的 HTTP 方法，如 GET、POST、PUT、DELETE 等。
     */
    @Schema(description = "HTTP 请求方法类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "POST")
    private String httpMethod;

    /**
     * 操作执行的时间。
     * <p>
     * 记录操作发生的具体时间点，用于时间序列分析和审计追踪。
     */
    @Schema(description = "操作执行的时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date time;

    /**
     * 操作执行的耗时（毫秒）。
     * <p>
     * 记录从方法开始执行到结束的总耗时，用于性能监控和分析。
     * 在 JSON 序列化时使用 "cost" 作为字段名。
     */
    @JsonProperty("cost")
    @Schema(description = "操作执行耗时（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "150")
    private long duration;

    /**
     * 操作的详细信息。
     * <p>
     * 包含操作的具体参数、返回结果等详细信息，通常以 JSON 格式存储。
     */
    @Schema(description = "操作的详细信息（JSON格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String detail;

    /**
     * 操作人员姓名。
     * <p>
     * 记录执行操作的用户姓名，用于审计和责任追踪。
     */
    @Schema(description = "操作人员姓名", example = "张三")
    private String operator;

    /**
     * 操作人员的唯一标识。
     * <p>
     * 记录执行操作的用户ID，用于精确的用户关联和权限审计。
     */
    @Schema(description = "操作人员的唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "user_12345")
    private String operatorId;

    /**
     * 租户标识。
     * <p>
     * 在多租户系统中用于标识操作所属的租户，实现数据隔离和权限控制。
     */
    @Schema(description = "租户标识", example = "tenant_001")
    private String tenantId;

    /**
     * 客户端 IP 地址。
     * <p>
     * 记录发起操作的客户端 IP 地址，用于安全审计和地理位置分析。
     */
    @Schema(description = "客户端 IP 地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "192.168.1.100")
    private String ipAddress;
}
