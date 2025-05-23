package com.lambda.autoconfig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author w
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "lambda.liquibase")
public class LiquibaseProperties {

    boolean enabled = true;
    String url;
    String username;
    String password;
    String driverClassName;
}
