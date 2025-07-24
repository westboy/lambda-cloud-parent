package com.lambda.cloud.core.exception.feign;

import com.lambda.cloud.core.exception.model.ErrorModel;
import lombok.Getter;

/**
 * Feign客户端异常基类
 * <p>
 * 该抽象类为所有Feign客户端相关的异常提供统一的基础结构。
 * 封装了HTTP请求失败时的通用信息，包括时间戳、错误类型、错误消息和请求路径。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>统一Feign异常的数据结构</li>
 *   <li>提供HTTP状态码的抽象获取方法</li>
 *   <li>封装错误模型的通用属性</li>
 *   <li>支持异常信息的标准化处理</li>
 * </ul>
 *
 * <h3>设计模式：</h3>
 * <p>采用模板方法模式，定义了异常处理的通用流程，子类只需实现具体的HTTP状态码。</p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * public class CustomFeignException extends AbstractFeignException {
 *     public CustomFeignException(ErrorModel model) {
 *         super(model);
 *     }
 *
 *     @Override
 *     public int getStatus() {
 *         return 400; // 具体的HTTP状态码
 *     }
 * }
 * }</pre>
 *
 * @author jin
 * @since 2025-07-23
 * @see ErrorModel
 * @see feign.FeignException
 */
public abstract class AbstractFeignException extends RuntimeException {

    /**
     * 异常发生的时间戳
     * <p>
     * 记录异常发生的具体时间，用于问题追踪和日志分析。
     * 时间戳采用毫秒级精度的Unix时间戳格式。
     * </p>
     */
    @Getter
    private final long timestamp;

    /**
     * 异常名称
     * <p>
     * 异常的类型名称或简短描述，如"Bad Request"、"Internal Server Error"等。
     * 通常对应HTTP状态码的标准描述。
     * </p>
     */
    @Getter
    private final String error;

    /**
     * 异常详细信息
     * <p>
     * 对异常的详细描述，包含具体的错误原因和上下文信息。
     * 该信息用于向用户展示或开发者调试。
     * </p>
     */
    private final String message;

    /**
     * 请求路径
     * <p>
     * 发生异常的HTTP请求路径，用于定位具体的API端点。
     * 包含完整的路径信息，便于问题排查。
     * </p>
     */
    @Getter
    private final String path;

    /**
     * 构造方法
     * <p>
     * 基于错误模型创建Feign异常实例。从错误模型中提取所有必要的异常信息，
     * 包括消息、路径、错误类型和时间戳。
     * </p>
     *
     * @param model 错误模型，包含完整的异常信息，不能为null
     * @throws NullPointerException 如果model为null
     */
    protected AbstractFeignException(ErrorModel model) {
        super(model.getMessage());
        this.path = model.getPath();
        this.error = model.getError();
        this.message = model.getMessage();
        this.timestamp = model.getTimestamp();
    }

    /**
     * 获取HTTP状态码
     * <p>
     * 抽象方法，由子类实现具体的HTTP状态码返回逻辑。
     * 不同类型的Feign异常应该返回对应的HTTP状态码。
     * </p>
     *
     * @return HTTP状态码，如400、401、500等
     */
    public abstract int getStatus();

    /**
     * 获取请求路径
     * <p>
     * 返回发生异常的HTTP请求路径。该路径信息来源于构造时传入的错误模型。
     * </p>
     *
     * @return 请求路径字符串，可能为null
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取异常消息
     * <p>
     * 重写父类方法，返回详细的异常消息。该消息来源于构造时传入的错误模型。
     * </p>
     *
     * @return 异常详细消息
     */
    @Override
    public String getMessage() {
        return message;
    }
}
