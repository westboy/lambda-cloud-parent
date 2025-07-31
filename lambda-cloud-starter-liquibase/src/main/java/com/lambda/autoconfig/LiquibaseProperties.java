package com.lambda.autoconfig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Liquibase配置属性类
 * <p>
 * 该类用于管理Liquibase数据库迁移工具的配置参数，通过Spring Boot的
 * {@code @ConfigurationProperties}注解自动绑定配置文件中的属性。
 *
 * <p>配置前缀为{@code lambda.liquibase}，支持以下配置项：
 * <ul>
 *   <li>{@code enabled} - 是否启用Liquibase，默认为true</li>
 *   <li>{@code url} - 数据库连接URL</li>
 *   <li>{@code username} - 数据库用户名</li>
 *   <li>{@code password} - 数据库密码</li>
 *   <li>{@code driverClassName} - 数据库驱动类名</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * lambda:
 *   liquibase:
 *     enabled: true
 *     url: jdbc:mysql://localhost:3306/test
 *     username: root
 *     password: password
 *     driver-class-name: com.mysql.cj.jdbc.Driver
 * </pre>
 *
 * @author westboy
 * @version 1.0.0
 * @since 2024-01-01
 * @see LiquibaseAutoConfiguration
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "lambda.liquibase")
public class LiquibaseProperties {

    /**
     * 是否启用Liquibase，默认为true
     */
    private boolean enabled = true;

    /**
     * 数据库连接URL
     */
    private String url;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 数据库驱动类名
     */
    private String driverClassName;
}
