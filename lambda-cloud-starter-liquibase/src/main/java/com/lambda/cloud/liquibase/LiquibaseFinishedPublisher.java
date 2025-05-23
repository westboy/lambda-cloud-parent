package com.lambda.cloud.liquibase;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author w
 */
@Slf4j
public class LiquibaseFinishedPublisher {

    private final DataSource dataSource;
    private final List<LiquibasePostExecutor> executors;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
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
