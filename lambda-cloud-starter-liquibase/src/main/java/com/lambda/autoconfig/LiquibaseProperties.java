package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author w
 */
@Slf4j
@Data
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "springboot properties class")
@ConfigurationProperties(prefix = "lambda.liquibase")
public class LiquibaseProperties {

    boolean enabled = true;
    String url;
    String username;
    String password;
    String driverClassName;
}
