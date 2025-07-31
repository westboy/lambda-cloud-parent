package com.lambda.cloud.liquibase;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Liquibase后置执行器，用于执行额外的数据库变更脚本
 * <p>
 * 该记录类提供了在主要Liquibase迁移完成后执行额外变更脚本的能力。
 * 每个执行器实例负责执行一个特定的变更日志文件。
 *
 * <p>主要特性：
 * <ul>
 *   <li>支持执行自定义的Liquibase变更日志</li>
 *   <li>自动生成唯一的执行上下文</li>
 *   <li>提供完整的错误处理和日志记录</li>
 *   <li>使用记录类型确保不可变性</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @Bean
 * public LiquibasePostExecutor customExecutor() {
 *     return new LiquibasePostExecutor("classpath:db/custom-changelog.xml");
 * }
 * }
 * </pre>
 *
 * <p>执行器会在{@link LiquibaseFinishedPublisher}中被自动调用。
 *
 * @param changelog 变更日志文件路径，不能为null或空字符串
 * @author westboy
 * @version 1.0.0
 * @since 2024-01-01
 * @see LiquibaseFinishedPublisher
 * @see SpringLiquibase
 */
@Slf4j
public record LiquibasePostExecutor(String changelog) {

    /**
     * 构造函数，验证changelog参数
     */
    public LiquibasePostExecutor {
        if (!StringUtils.hasText(changelog)) {
            throw new IllegalArgumentException("Changelog cannot be null or empty");
        }
    }

    /**
     * 执行Liquibase变更脚本
     *
     * @param dataSource 数据源
     * @throws RuntimeException 当执行失败时抛出
     */
    public void execute(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }

        log.info("Executing Liquibase changelog: {}", changelog);

        try {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setResourceLoader(SpringUtil.getApplicationContext());
            liquibase.setChangeLog(changelog);
            liquibase.setDataSource(dataSource);
            liquibase.setContexts(IdUtil.fastSimpleUUID());
            liquibase.afterPropertiesSet();

            log.info("Successfully executed Liquibase changelog: {}", changelog);
        } catch (Exception e) {
            log.error("Failed to execute Liquibase changelog: {}", changelog, e);
            throw new RuntimeException("Failed to execute Liquibase changelog: " + changelog, e);
        }
    }
}
