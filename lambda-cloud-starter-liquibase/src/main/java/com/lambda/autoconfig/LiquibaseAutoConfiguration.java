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
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

/**
 * LiquibaseAutoConfiguration
 *
 * @author w
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
        String url = properties.getUrl();
        String username = properties.getUsername();
        String password = properties.getPassword();
        String driver = properties.getDriverClassName();
        DataSource dataSource = DataSourceUtils.getInstance(url, username, password, driver);
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:META-INF/db/changelogs/lambda-master.xml");
        liquibase.setDataSource(dataSource);
        liquibase.setContexts("lambda_cloud_liquibase");
        return liquibase;
    }

    @Bean
    @DependsOn("lambdaLiquibase")
    public LiquibaseFinishedPublisher liquibaseFinishedPublisher(
            SpringLiquibase jfLiquibase, List<LiquibasePostExecutor> executors) {
        DataSource dataSource = jfLiquibase.getDataSource();
        return new LiquibaseFinishedPublisher(dataSource, executors);
    }
}
