package com.jingfang.cloud.liquibase;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author w
 */
@Slf4j
@Setter
@Getter
@ConfigurationProperties(prefix = "jingfang.liquibase")
public class LiquibaseProperties {

    boolean enabled = true;
    String url;
    String username;
    String password;
    String driverClassName;

}