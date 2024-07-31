package com.jingfang.cloud.liquibase;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;

/**
 * @author w
 */
@Slf4j
public class LiquibaseFinishedPublisher {

    private final DataSource dataSource;
    private final List<LiquibasePostExecutor> executors;

    public LiquibaseFinishedPublisher(DataSource dataSource, List<LiquibasePostExecutor> executors) {
        this.dataSource = dataSource;
        this.executors = executors;
    }

    @PostConstruct
    public void execute() {
        if (CollectionUtils.isNotEmpty(executors)) {
            executors.forEach(item -> item.execute(dataSource));
        }
    }

}