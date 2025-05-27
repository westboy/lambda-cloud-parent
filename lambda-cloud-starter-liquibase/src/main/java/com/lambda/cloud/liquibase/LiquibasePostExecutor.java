package com.lambda.cloud.liquibase;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.SneakyThrows;

/**
 * @author w
 */
public class LiquibasePostExecutor {

    private final String changelog;

    public LiquibasePostExecutor(String changelog) {
        this.changelog = changelog;
    }

    @SneakyThrows
    public void execute(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(SpringUtil.getApplicationContext());
        liquibase.setChangeLog(changelog);
        liquibase.setDataSource(dataSource);
        liquibase.setContexts(IdUtil.fastSimpleUUID());
        liquibase.afterPropertiesSet();
    }
}
