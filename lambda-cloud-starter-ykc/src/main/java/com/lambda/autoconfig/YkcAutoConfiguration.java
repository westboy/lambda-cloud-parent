package com.lambda.autoconfig;

import com.lambda.cloud.ykc.message.v17.req.*;
import com.lambda.cloud.ykc.message.v17.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 云快充协议自动配置类
 * <p>
 * 负责自动装配云快充V1.7协议相关的消息类，包括：
 * <ul>
 *     <li>登录消息 (0x01, 0x02)</li>
 *     <li>心跳消息 (0x03, 0x04)</li>
 *     <li>计费模型消息 (0x09, 0x0A)</li>
 *     <li>监测数据消息 (0x12, 0x13)</li>
 * </ul>
 * </p>
 *
 * @author Generated
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
public class YkcAutoConfiguration {

    public YkcAutoConfiguration() {
        log.info("YkcAutoConfiguration initialized - 云快充协议自动配置类已初始化");
    }

    // ==================== 登录消息 ====================

    @Bean
    public YkcV17LoginRequestMessage ykcV17LoginRequestMessage() {
        return new YkcV17LoginRequestMessage();
    }

    @Bean
    public YkcV17LoginRequestDetail ykcV17LoginRequestDetail() {
        return new YkcV17LoginRequestDetail();
    }

    @Bean
    public YkcV17LoginResponseMessage ykcV17LoginResponseMessage() {
        return new YkcV17LoginResponseMessage();
    }

    @Bean
    public YkcV17LoginResponseDetail ykcV17LoginResponseDetail() {
        return new YkcV17LoginResponseDetail();
    }

    // ==================== 心跳消息 ====================

    @Bean
    public YkcV17HeartbeatRequestMessage ykcV17HeartbeatRequestMessage() {
        return new YkcV17HeartbeatRequestMessage();
    }

    @Bean
    public YkcV17HeartbeatRequestDetail ykcV17HeartbeatRequestDetail() {
        return new YkcV17HeartbeatRequestDetail();
    }

    @Bean
    public YkcV17HeartbeatResponseMessage ykcV17HeartbeatResponseMessage() {
        return new YkcV17HeartbeatResponseMessage();
    }

    @Bean
    public YkcV17HeartbeatResponseDetail ykcV17HeartbeatResponseDetail() {
        return new YkcV17HeartbeatResponseDetail();
    }

    // ==================== 计费模型消息 ====================

    @Bean
    public YkcV17BillingModelRequestMessage ykcV17BillingModelRequestMessage() {
        return new YkcV17BillingModelRequestMessage();
    }

    @Bean
    public YkcV17BillingModelRequestDetail ykcV17BillingModelRequestDetail() {
        return new YkcV17BillingModelRequestDetail();
    }

    @Bean
    public YkcV17BillingModelResponseMessage ykcV17BillingModelResponseMessage() {
        return new YkcV17BillingModelResponseMessage();
    }

    @Bean
    public YkcV17BillingModelResponseDetail ykcV17BillingModelResponseDetail() {
        return new YkcV17BillingModelResponseDetail();
    }

    // ==================== 监测数据消息 ====================

    @Bean
    public YkcV17MonitoringDataRequestMessage ykcV17MonitoringDataRequestMessage() {
        return new YkcV17MonitoringDataRequestMessage();
    }

    @Bean
    public YkcV17MonitoringDataRequestDetail ykcV17MonitoringDataRequestDetail() {
        return new YkcV17MonitoringDataRequestDetail();
    }

    @Bean
    public YkcV17MonitoringDataResponseMessage ykcV17MonitoringDataResponseMessage() {
        return new YkcV17MonitoringDataResponseMessage();
    }

    @Bean
    public YkcV17MonitoringDataResponseDetail ykcV17MonitoringDataResponseDetail() {
        return new YkcV17MonitoringDataResponseDetail();
    }
}
