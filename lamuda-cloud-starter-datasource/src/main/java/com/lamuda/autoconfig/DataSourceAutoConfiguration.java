package com.lamuda.autoconfig;


import com.lamuda.cloud.datasource.config.DynamicDataSourceConfigurer;
import com.lamuda.cloud.datasource.config.StandardDataSourceConfigurer;
import com.lamuda.cloud.datasource.dynamic.DynamicDataSourceService;
import com.lamuda.cloud.datasource.dynamic.impl.DynamicDataSourceServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author a
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class)
@Import({StandardDataSourceConfigurer.class, DynamicDataSourceConfigurer.class})
public class DataSourceAutoConfiguration {

    public DataSourceAutoConfiguration() {
        log.trace("DataSourceAutoConfiguration initializing...");
    }


    @Bean
    public DynamicDataSourceService dynamicDataSource() {
        return new DynamicDataSourceServiceImpl();
    }
}
