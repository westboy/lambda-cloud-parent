package com.lambda.cloud.logger.service.impl;

import static com.lambda.cloud.core.Constants.GSON;

import com.lambda.cloud.logger.model.OperationLogRecord;
import com.lambda.cloud.logger.service.OperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * 默认操作日志服务实现类。
 * <p>
 * 该类提供了 {@link OperationService} 接口的默认实现，
 * 采用简单的日志输出方式来处理操作日志。主要用于：
 * <ul>
 *     <li>开发和测试环境的日志记录</li>
 *     <li>作为自定义实现的参考模板</li>
 *     <li>在没有特殊存储需求时的默认选择</li>
 * </ul>
 * <p>
 * 该实现将操作日志以 JSON 格式输出到应用日志中，
 * 生产环境建议根据实际需求实现自定义的 {@link OperationService}。
 * <p>
 * 特性：
 * <ul>
 *     <li>线程安全：使用无状态设计</li>
 *     <li>高性能：直接输出到日志，无额外 I/O 开销</li>
 *     <li>易于调试：日志内容清晰可读</li>
 * </ul>
 *
 * @author jpjoo
 * @since 1.0.0
 * @see OperationService
 */
@Slf4j
public class DefaultOperationServiceImpl implements OperationService {

    /**
     * 保存操作日志到应用日志中。
     * <p>
     * 该方法将操作日志对象序列化为 JSON 格式，并通过 SLF4J 日志框架
     * 以 INFO 级别输出。日志格式为：
     * <pre>
     * save OPERATION LOGGER: {"id":"...","method":"...","module":"...",...}
     * </pre>
     * <p>
     * 方法特点：
     * <ul>
     *     <li>同步执行：直接在当前线程中完成日志输出</li>
     *     <li>异常安全：内部异常不会影响业务流程</li>
     *     <li>参数校验：确保传入参数的有效性</li>
     * </ul>
     *
     * @param operationLogRecord 操作日志记录对象，不能为 {@code null}
     * @throws IllegalArgumentException 如果 operationLogRecord 为 {@code null}
     */
    @Override
    public void save(OperationLogRecord operationLogRecord) {
        Assert.notNull(operationLogRecord, "Operation log record must not be null");

        try {
            // 记录操作日志到控制台（实际项目中可替换为数据库、消息队列等）
            log.info("Save operation log: {}", GSON.toJson(operationLogRecord));
        } catch (Exception e) {
            // 确保日志记录异常不影响业务流程
            log.warn("Failed to save operation log: {}", e.getMessage(), e);
        }
    }
}
