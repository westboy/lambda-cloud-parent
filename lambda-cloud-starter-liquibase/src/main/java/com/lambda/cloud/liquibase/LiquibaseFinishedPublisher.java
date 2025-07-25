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
@SuppressFBWarnings("EI_EXPOSE_REP")
public record LiquibaseFinishedPublisher(DataSource dataSource, List<LiquibasePostExecutor> executors) {

    @PostConstruct
    public void execute() {
        if (CollectionUtils.isNotEmpty(executors)) {
            executors.forEach(item -> item.execute(dataSource));
        }
    }
}
