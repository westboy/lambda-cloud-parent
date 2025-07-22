package com.lambda.cloud.liquibase;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author w
 */
@Slf4j
public record LiquibaseFinishedPublisher(DataSource dataSource, List<LiquibasePostExecutor> executors) {

    @PostConstruct
    public void execute() {
        if (CollectionUtils.isNotEmpty(executors)) {
            executors.forEach(item -> item.execute(dataSource));
        }
    }
}
