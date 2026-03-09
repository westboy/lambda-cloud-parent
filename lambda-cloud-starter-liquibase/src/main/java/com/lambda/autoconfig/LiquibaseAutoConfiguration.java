package com.lambda.autoconfig;

import com.lambda.cloud.datasource.utils.DataSourceUtils;
import com.lambda.cloud.liquibase.LiquibaseFinishedPublisher;
import com.lambda.cloud.liquibase.LiquibasePostExecutor;
import java.util.List;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

/**
 * Liquibase自动配置类
 * <p>
 * 该类负责自动配置Liquibase数据库迁移工具，提供以下功能：
 * <ul>
 *   <li>自动创建SpringLiquibase实例</li>
 *   <li>配置数据库连接参数</li>
 *   <li>管理变更日志文件的执行</li>
 *   <li>提供后置处理器支持</li>
 * </ul>
 *
 * <p>配置属性通过{@link LiquibaseProperties}进行管理，支持通过
 * {@code lambda.liquibase.enabled}属性控制是否启用。
 *
 * @author westboy
 * @version 1.0.0
 * @see LiquibaseProperties
 * @see LiquibaseFinishedPublisher
 * @see SpringLiquibase
 * @since 2024-01-01
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(LiquibaseProperties.class)
@ConditionalOnProperty(prefix = "lambda.liquibase", name = "enabled", matchIfMissing = true)
public class LiquibaseAutoConfiguration {

    public LiquibaseAutoConfiguration() {
        log.trace("LiquibaseAutoConfiguration initializing...");
    }

    @Primary
    @Bean("lambdaLiquibase")
    public SpringLiquibase liquibase(LiquibaseProperties properties) {
        // 参数验证
        if (properties == null) {
            throw new IllegalArgumentException("LiquibaseProperties cannot be null");
        }

        String url = properties.getUrl();
        String username = properties.getUsername();
        String password = properties.getPassword();
        String driver = properties.getDriverClassName();

        // 验证必要的数据库连接参数
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("Database URL cannot be null or empty");
        }
        if (!StringUtils.hasText(driver)) {
            throw new IllegalArgumentException("Database driver class name cannot be null or empty");
        }

        log.info("Initializing Liquibase with URL: {}, Driver: {}", url, driver);

        try {
            DataSource dataSource = DataSourceUtils.getInstance(url, username, password, driver);
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setChangeLog("classpath:META-INF/db/changelogs/lambda-master.xml");
            liquibase.setDataSource(dataSource);
            liquibase.setContexts("lambda_cloud_liquibase");
            return liquibase;
        } catch (Exception e) {
            log.error("Failed to initialize Liquibase with URL: {}", url, e);
            throw new RuntimeException("Failed to initialize Liquibase", e);
        }
    }

    @Bean
    @DependsOn("lambdaLiquibase")
    public LiquibaseFinishedPublisher liquibaseFinishedPublisher(
            SpringLiquibase lambdaLiquibase, List<LiquibasePostExecutor> executors) {
        if (lambdaLiquibase == null) {
            throw new IllegalArgumentException("SpringLiquibase cannot be null");
        }

        DataSource dataSource = lambdaLiquibase.getDataSource();
        if (dataSource == null) {
            throw new IllegalStateException("DataSource from SpringLiquibase cannot be null");
        }

        log.debug("Creating LiquibaseFinishedPublisher with {} executors", executors != null ? executors.size() : 0);

        return new LiquibaseFinishedPublisher(dataSource, executors);
    }
}
