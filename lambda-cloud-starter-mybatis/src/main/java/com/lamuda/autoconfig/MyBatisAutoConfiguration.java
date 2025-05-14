package com.lambda.autoconfig;


import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.lambda.cloud.mybatis.extend.ExtendLogicSqlInjector;
import com.lambda.cloud.mybatis.interceptor.InsertBatchInterceptor;
import com.lambda.cloud.mybatis.handler.GlobalMetaObjectHandler;
import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;


import javax.sql.DataSource;
import java.util.Properties;

/**
 * MyBatisPlusConfig
 *
 * @author Jin
 */
@Slf4j
@Configuration
@MapperScan("${mybatis-plus.mapperPackage:com.lambda.cloud.**.mapper}")
@Import({MybatisPlusAutoConfiguration.class})
@EnableConfigurationProperties(MybatisPlusExtendProperties.class)
@AutoConfigureAfter(value = DataSourceAutoConfiguration.class)
public class MyBatisAutoConfiguration {


    public MyBatisAutoConfiguration() {
        log.trace("initializing...");
    }

    @Bean
    public GlobalMetaObjectHandler globalMetaObjectHandler() {
        return new GlobalMetaObjectHandler();
    }

    @Bean
    public DatabaseIdProvider databaseIdProvider(MybatisPlusExtendProperties mybatisProperties) {
        Properties properties = new Properties();
        properties.setProperty("H2", "h2");
        properties.setProperty("DB2", "db2");
        properties.setProperty("MySQL", "mysql");
        properties.setProperty("Oracle", "oracle");
        properties.setProperty("PostgreSQL", "postgresql");
        properties.setProperty("DM DBMS", "oracle");
        if (mybatisProperties.getDatabaseIdMap() != null) {
            properties.putAll(mybatisProperties.getDatabaseIdMap());
        }
        VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }
    @Bean
    public ExtendLogicSqlInjector extendLogicSqlInjector() {
        return new ExtendLogicSqlInjector();
    }


    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(@Lazy DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 批量插入攔截器
     *
     * @return
     */
    @Bean
    @Order(30)
    public InsertBatchInterceptor insertBatchInterceptor() {
        return new InsertBatchInterceptor();
    }


    /**
     * 解决Oracle批量插值NULL转换问题
     *
     * @return 自定义配置项
     */
    @Bean
    public ConfigurationCustomizer setJdbcTypeForNull() {
        return configuration -> configuration.setJdbcTypeForNull(JdbcType.NULL);
    }

    /***
     * 分页拦截器
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

}
