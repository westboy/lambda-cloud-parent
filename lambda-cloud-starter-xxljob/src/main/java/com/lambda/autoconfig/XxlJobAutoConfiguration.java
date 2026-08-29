package com.lambda.autoconfig;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * XXL-Job 执行器自动装配。
 * <p>仅当 {@code lambda.cloud.xxl-job.enabled=true} 时装配 {@link XxlJobSpringExecutor}，
 * 默认关闭，避免无调度中心的环境启动报错。调度参数（cron、路由、重试、超时）在调度中心
 * 配置，本自动配置只装配执行器连接（工程契约 §23.3.2）。</p>
 *
 * @author Jin
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "lambda.cloud.xxl-job", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(XxlJobProperties.class)
public class XxlJobAutoConfiguration {

    /**
     * 装配 XXL-Job 执行器，扫描本服务内 {@code @XxlJob} 注解的 handler 并注册到调度中心。
     *
     * @param properties 执行器连接参数
     * @return XXL-Job Spring 执行器
     */
    @Bean
    public XxlJobSpringExecutor xxlJobSpringExecutor(XxlJobProperties properties) {
        log.info(
                "[xxl-job] init executor, adminAddresses={}, appname={}",
                properties.getAdminAddresses(),
                properties.getAppname());
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(properties.getAdminAddresses());
        executor.setAccessToken(properties.getAccessToken());
        executor.setAppname(properties.getAppname());
        executor.setAddress(properties.getAddress());
        executor.setIp(properties.getIp());
        executor.setPort(properties.getPort());
        executor.setLogPath(properties.getLogPath());
        executor.setLogRetentionDays(properties.getLogRetentionDays());
        return executor;
    }
}
