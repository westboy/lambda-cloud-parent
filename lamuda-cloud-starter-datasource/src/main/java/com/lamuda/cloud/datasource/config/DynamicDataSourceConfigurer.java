package com.lamuda.cloud.datasource.config;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import com.lamuda.cloud.datasource.condition.DynamicDataSourceCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author w
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@Conditional(DynamicDataSourceCondition.class)
@Import(DynamicDataSourceAutoConfiguration.class)
public class DynamicDataSourceConfigurer {

    public DynamicDataSourceConfigurer(){
        log.trace("DynamicDataSourceConfigurer initializing...");
    }

}