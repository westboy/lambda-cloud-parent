package com.lambda.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.ocpp.action.OcppAction;
import com.lambda.cloud.ocpp.action.OcppActionRegistry;
import com.lambda.cloud.ocpp.codec.OcppMessageCodec;
import com.lambda.cloud.ocpp.message.v16.authorize.AuthorizeRequest;
import com.lambda.cloud.ocpp.message.v16.authorize.AuthorizeResponse;
import com.lambda.cloud.ocpp.message.v16.bootnotification.BootNotificationRequest;
import com.lambda.cloud.ocpp.message.v16.bootnotification.BootNotificationResponse;
import com.lambda.cloud.ocpp.message.v16.heartbeat.HeartbeatRequest;
import com.lambda.cloud.ocpp.message.v16.heartbeat.HeartbeatResponse;
import com.lambda.cloud.ocpp.message.v16.remotestarttransaction.RemoteStartTransactionRequest;
import com.lambda.cloud.ocpp.message.v16.remotestarttransaction.RemoteStartTransactionResponse;
import com.lambda.cloud.ocpp.message.v16.remotestoptransaction.RemoteStopTransactionRequest;
import com.lambda.cloud.ocpp.message.v16.remotestoptransaction.RemoteStopTransactionResponse;
import com.lambda.cloud.ocpp.message.v16.starttransaction.StartTransactionRequest;
import com.lambda.cloud.ocpp.message.v16.starttransaction.StartTransactionResponse;
import com.lambda.cloud.ocpp.message.v16.statusnotification.StatusNotificationRequest;
import com.lambda.cloud.ocpp.message.v16.statusnotification.StatusNotificationResponse;
import com.lambda.cloud.ocpp.message.v16.stoptransaction.StopTransactionRequest;
import com.lambda.cloud.ocpp.message.v16.stoptransaction.StopTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * OCPP 协议 starter 自动配置。
 * <p>注册内置 OCPP 1.6 action 的请求/响应类型到 {@link OcppActionRegistry},
 * 并提供无状态 {@link OcppMessageCodec}(复用 Spring Boot 的 {@link ObjectMapper})。</p>
 * <p>编解码器复用应用已有的 {@link ObjectMapper}(Spring Boot 自动配置,含 record 支持);
 * 不注册 netty handler bean,WebSocket pipeline 装配由下游运行时(如 chargemind-iot-ocpp)负责。</p>
 *
 * @see OcppProperties
 * @see OcppActionRegistry
 * @see OcppMessageCodec
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(OcppProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lambda.ocpp", name = "enabled", matchIfMissing = true)
public class OcppAutoConfiguration {

    /**
     * OCPP action 注册表,内置 1.6 核心 action。
     */
    @Bean
    @ConditionalOnMissingBean
    public OcppActionRegistry ocppActionRegistry() {
        OcppActionRegistry registry = new OcppActionRegistry();
        registerBuiltinPayloads(registry);
        return registry;
    }

    /**
     * OCPP-J JSON 编解码器。复用 Spring Boot 的 {@link ObjectMapper}。
     */
    @Bean
    @ConditionalOnMissingBean
    public OcppMessageCodec ocppMessageCodec(ObjectMapper objectMapper, OcppActionRegistry registry) {
        return new OcppMessageCodec(objectMapper, registry);
    }

    /**
     * 注册 OCPP 1.6 核心 action 的请求/响应类型。
     */
    private void registerBuiltinPayloads(OcppActionRegistry registry) {
        registry.register(OcppAction.BOOT_NOTIFICATION, BootNotificationRequest.class, BootNotificationResponse.class);
        registry.register(OcppAction.AUTHORIZE, AuthorizeRequest.class, AuthorizeResponse.class);
        registry.register(OcppAction.START_TRANSACTION, StartTransactionRequest.class, StartTransactionResponse.class);
        registry.register(OcppAction.STOP_TRANSACTION, StopTransactionRequest.class, StopTransactionResponse.class);
        registry.register(OcppAction.HEARTBEAT, HeartbeatRequest.class, HeartbeatResponse.class);
        registry.register(
                OcppAction.STATUS_NOTIFICATION, StatusNotificationRequest.class, StatusNotificationResponse.class);
        registry.register(
                OcppAction.REMOTE_START_TRANSACTION,
                RemoteStartTransactionRequest.class,
                RemoteStartTransactionResponse.class);
        registry.register(
                OcppAction.REMOTE_STOP_TRANSACTION,
                RemoteStopTransactionRequest.class,
                RemoteStopTransactionResponse.class);
        log.info("Registered 8 built-in OCPP 1.6 actions");
    }
}
