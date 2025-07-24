package com.lambda.cloud.logger.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，用于标记需要记录操作日志的方法。
 * <p>
 * 该注解可以应用于任何需要记录操作日志的方法上，
 * 配合 {@code OperationLoggerAdvice} 切面使用，自动记录方法的执行信息。
 * <p>
 * 使用示例：
 * <pre>
 * {@code @OperationLog(value = "用户登录", module = "用户管理", type = "LOGIN")}
 * public LoginResult login(LoginRequest request) {
 *     // 登录逻辑
 * }
 * </pre>
 * <p>
 * 注解支持的配置项：
 * <ul>
 *     <li>{@link #value()} - 操作标识，用于描述具体的操作内容</li>
 *     <li>{@link #module()} - 所属模块，用于分类管理操作日志</li>
 *     <li>{@link #type()} - 操作类型，用于标识操作的业务类型</li>
 * </ul>
 *
 * @author jpjoo
 * @since 1.0.0
 * @see com.lambda.cloud.logger.advices.OperationLoggerAdvice
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperationLog {

    /**
     * 操作标识。
     * <p>
     * 用于标识具体的操作内容，如 "用户登录"、"数据导出" 等。
     * 如果不指定，将使用方法的全限定名作为默认值。
     *
     * @return 操作标识字符串，默认为空字符串
     */
    String value() default "";

    /**
     * 所属模块名称。
     * <p>
     * 用于对操作日志进行模块化分类管理，便于后续的日志查询和统计分析。
     * 建议使用有意义的模块名称，如 "用户管理"、"订单管理" 等。
     *
     * @return 模块名称，默认为 "模块"
     */
    String module() default "模块";

    /**
     * 操作类型。
     * <p>
     * 用于标识操作的业务类型，如 "CREATE"、"UPDATE"、"DELETE"、"QUERY" 等。
     * 如果不指定，将根据 HTTP 方法自动推断操作类型。
     *
     * @return 操作类型字符串，默认为空字符串
     */
    String type() default "";
}
