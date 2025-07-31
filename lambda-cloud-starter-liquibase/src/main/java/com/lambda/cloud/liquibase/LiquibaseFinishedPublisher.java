package com.lambda.cloud.liquibase;

import com.lambda.autoconfig.LiquibaseAutoConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Liquibase完成后的发布器，用于执行后置处理器
 * <p>
 * 该记录类在Liquibase主要迁移任务完成后，自动执行所有注册的后置处理器。
 * 通过{@code @PostConstruct}注解确保在Bean初始化完成后立即执行。
 *
 * <p>主要功能：
 * <ul>
 *   <li>管理和执行所有{@link LiquibasePostExecutor}实例</li>
 *   <li>提供执行统计和错误处理</li>
 *   <li>确保单个执行器失败不影响其他执行器</li>
 *   <li>记录详细的执行日志</li>
 * </ul>
 *
 * <p>该类使用记录类型(record)实现，确保数据的不可变性和线程安全。
 *
 * @param dataSource 数据源，不能为null
 * @param executors 后置执行器列表，可以为空或null
 * @author westboy
 * @version 1.0.0
 * @since 2024-01-01
 * @see LiquibasePostExecutor
 * @see LiquibaseAutoConfiguration
 */
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP")
public record LiquibaseFinishedPublisher(DataSource dataSource, List<LiquibasePostExecutor> executors) {

    /**
     * 构造函数，验证参数
     */
    public LiquibaseFinishedPublisher {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
    }

    /**
     * 在Bean初始化完成后执行所有后置处理器
     */
    @PostConstruct
    public void execute() {
        if (CollectionUtils.isEmpty(executors)) {
            log.info("No Liquibase post executors found, skipping post-processing");
            return;
        }

        log.info("Starting execution of {} Liquibase post executors", executors.size());

        int successCount = 0;
        int failureCount = 0;

        for (LiquibasePostExecutor executor : executors) {
            try {
                if (executor != null) {
                    executor.execute(dataSource);
                    successCount++;
                } else {
                    log.warn("Encountered null executor, skipping");
                }
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to execute Liquibase post executor: {}", executor, e);
                // 继续执行其他执行器，不因为一个失败而中断整个流程
            }
        }

        log.info("Completed Liquibase post-processing. Success: {}, Failures: {}", successCount, failureCount);
    }
}
