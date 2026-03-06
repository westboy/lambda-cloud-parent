package com.lambda.autoconfig;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.lambda.autoconfig.condition.MapperPackageConfiguredCondition;
import com.lambda.cloud.mybatis.handler.AesEncryptHandler;
import com.lambda.cloud.mybatis.handler.EntityMetaFiller;
import com.lambda.cloud.mybatis.handler.GlobalMetaObjectHandler;
import com.lambda.cloud.mybatis.injector.LambdaExtendSqlInjector;
import com.lambda.cloud.mybatis.tenant.TenantExpressionInterceptor;
import com.lambda.cloud.mybatis.tenant.TenantHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MyBatisPlusConfig
 *
 * @author Jin
 */
@Slf4j
@Configuration
@Import({MybatisPlusAutoConfiguration.class})
@EnableConfigurationProperties(MybatisPlusExtendProperties.class)
@AutoConfigureAfter(value = DataSourceAutoConfiguration.class)
public class MyBatisAutoConfiguration {

    public MyBatisAutoConfiguration() {
        log.trace("MyBatisAutoConfiguration initializing...");
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

    /**
     * 动态注册 MapperScannerConfigurer Bean
     */
    @Bean
    @Conditional(MapperPackageConfiguredCondition.class)
    public MapperScannerConfigurer mapperScannerConfigurer(Environment env) {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        String property = env.getProperty("mybatis-plus.mapper-package");
        configurer.setBasePackage(property);
        log.info("注册 MapperScannerConfigurer，扫描包：{}", property);
        return configurer;
    }

    /**
     * 全局元对象处理器
     *
     * @return GlobalMetaObjectHandler
     */
    @Bean
    public GlobalMetaObjectHandler globalMetaObjectHandler(
            @Autowired(required = false) List<EntityMetaFiller> entityMetaFillers) {
        return new GlobalMetaObjectHandler(entityMetaFillers);
    }

    /**
     * 数据库类型处理器
     *
     * @param mybatisProperties mybatisProperties
     * @return DatabaseIdProvider
     */
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

    /**
     * 自定义方法
     *
     * @return ExtendLogicSqlInjector
     */
    @Bean
    public LambdaExtendSqlInjector extendLogicSqlInjector() {
        return new LambdaExtendSqlInjector();
    }

    /**
     * jdbcTemplate
     *
     * @param dataSource dataSource
     * @return JdbcTemplate
     */
    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(@Lazy DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /***
     * Mybatis拦截器
     * @return mybatisPlusInterceptor
     */
    @Bean
    @Order(0)
    public MybatisPlusInterceptor mybatisPlusInterceptor(List<InnerInterceptor> innerInterceptors) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.setInterceptors(innerInterceptors);
        return interceptor;
    }

    /***
     * 分页拦截器
     * @return mybatisPlusInterceptor
     */
    @Bean
    @Order(20)
    public InnerInterceptor pageInnerInterceptor() {
        return new PaginationInnerInterceptor();
    }

    /**
     * 加密解密
     */
    @Configuration
    @ConditionalOnProperty(prefix = "mybatis-plus.encrypt", name = "enabled")
    public static class EncryptConfig {
        @SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
        private MybatisPlusExtendProperties mybatisProperties;

        @Autowired
        public void setMybatisProperties(MybatisPlusExtendProperties mybatisProperties) {
            this.mybatisProperties = mybatisProperties;
        }

        @Bean
        public AesEncryptHandler aesEncryptHandler() {
            return new AesEncryptHandler(mybatisProperties.getEncrypt().getKey());
        }

        @Bean
        public ConfigurationCustomizer registerAesEncryptHandler(AesEncryptHandler aesEncryptHandler) {
            return configuration -> configuration.getTypeHandlerRegistry().register(aesEncryptHandler);
        }
    }

    /**
     * Tenant配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "mybatis-plus.tenant", name = "enabled")
    public static class TenantConfig {

        @Bean
        @Order(9)
        @ConditionalOnMissingBean
        public TenantLineHandler tenantLineHandler(MybatisPlusExtendProperties mybatisProperties) {
            return new TenantHandler(mybatisProperties);
        }

        @Bean
        @Order(10)
        public InnerInterceptor tenantLineInnerInterceptor(TenantLineHandler tenantLineHandler) {
            TenantLineInnerInterceptor tenantLineInnerInterceptor = new TenantLineInnerInterceptor();
            tenantLineInnerInterceptor.setTenantLineHandler(tenantLineHandler);
            return tenantLineInnerInterceptor;
        }

        @Bean
        @Order(Short.MAX_VALUE)
        public TenantExpressionInterceptor tenantExpressionInterceptor(MybatisPlusExtendProperties mybatisProperties) {
            String name = mybatisProperties.getTenant().getTenantColumn();
            return new TenantExpressionInterceptor(name);
        }
    }
}
