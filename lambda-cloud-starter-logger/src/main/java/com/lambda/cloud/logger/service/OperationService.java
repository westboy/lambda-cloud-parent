package com.lambda.cloud.logger.service;

import com.lambda.cloud.logger.model.OperationLogRecord;

/**
 * 操作日志服务接口。
 * <p>
 * 该接口定义了操作日志的核心服务方法，主要用于操作日志的持久化处理。
 * 实现类可以根据具体需求选择不同的存储方式，如：
 * <ul>
 *     <li>数据库存储</li>
 *     <li>文件系统存储</li>
 *     <li>消息队列发送（如 Kafka）</li>
 *     <li>远程服务调用</li>
 * </ul>
 * <p>
 * 框架提供了默认实现 {@link DefaultOperationServiceImpl}，
 * 用户可以通过实现此接口来自定义日志处理逻辑。
 *
 * @author jpjoo
 * @since 1.0.0
 * @see DefaultOperationServiceImpl
 * @see OperationLogRecord
 */
public interface OperationService {

    /**
     * 保存操作日志。
     * <p>
     * 该方法负责处理操作日志的持久化或传输，具体的实现方式由实现类决定。
     * 方法应该是异步非阻塞的，避免影响业务方法的执行性能。
     * <p>
     * 实现注意事项：
     * <ul>
     *     <li>方法不应抛出异常，避免影响业务流程</li>
     *     <li>建议使用异步处理，提高性能</li>
     *     <li>应处理好异常情况，确保日志记录的可靠性</li>
     *     <li>考虑日志量大的情况，做好性能优化</li>
     * </ul>
     *
     * @param operationLogRecord 操作日志记录对象，包含完整的操作信息
     * @throws IllegalArgumentException 如果 operationLogRecord 为 null
     */
    void save(OperationLogRecord operationLogRecord);
}
