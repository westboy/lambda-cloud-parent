package com.lambda.autoconfig;

import com.lambda.cloud.netty.protocol.scanner.ProtocolPayloadScanner;
import com.lambda.cloud.t645.message.T645PayloadRegistry;
import com.lambda.cloud.t645.message.control.T645BroadcastTime;
import com.lambda.cloud.t645.message.read.T645ReadAddressRequest;
import com.lambda.cloud.t645.message.read.T645ReadAddressResponse;
import com.lambda.cloud.t645.message.read.T645ReadEnergyRequest;
import com.lambda.cloud.t645.message.read.T645ReadEnergyResponse;
import com.lambda.cloud.t645.message.heartbeat.T645HeartbeatRequest;
import com.lambda.cloud.t645.message.heartbeat.T645HeartbeatResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * DL/T 645-2007 多功能电能表通信协议自动配置类。
 *
 * <p>在 Spring Boot 应用启动完成后，根据配置自动扫描并注册 T645 协议报文载荷类，
 * 同时注册内置的标准报文（读电能、读地址、广播校时、心跳等）。</p>
 *
 * <p>配置前缀：{@code lambda.t645.protocol}</p>
 *
 * @see T645Properties
 * @see T645PayloadRegistry
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(T645Properties.class)
@RequiredArgsConstructor
public class T645AutoConfiguration {

    private final T645Properties properties;

    /**
     * 协议载荷扫描器，用于扫描带有 {@code @ProtocolPayload} 注解的报文类。
     */
    @Bean
    public ProtocolPayloadScanner t645ProtocolPayloadScanner() {
        return new ProtocolPayloadScanner();
    }

    /**
     * 应用启动就绪监听器，在 Spring 上下文初始化完成后执行协议载荷扫描与内置报文注册。
     *
     * <p>扫描流程：
     * <ol>
     *   <li>检查 {@code lambda.t645.protocol.enabled} 是否启用</li>
     *   <li>检查 {@code lambda.t645.protocol.lazy-init} 是否延迟初始化</li>
     *   <li>扫描指定包路径下的 {@code @ProtocolPayload} 注解类</li>
     *   <li>注册 DL/T 645-2007 内置报文映射</li>
     * </ol>
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> t645ScannerListener(ProtocolPayloadScanner scanner) {
        return event -> {
            if (!properties.isEnabled()) {
                log.info("T645 protocol scanning is disabled");
                return;
            }

            if (properties.isLazyInit()) {
                log.info("T645 protocol scanner lazy initialization is enabled, skipping auto-scan");
                return;
            }

            String[] basePackages = properties.getBasePackages();
            if (basePackages == null || basePackages.length == 0) {
                basePackages = new String[] {"com.lambda.cloud.t645.message"};
                log.info("No T645 base packages configured, using default: {}", String.join(", ", basePackages));
            }

            log.info("Starting T645 protocol auto-scan with packages: {}", String.join(", ", basePackages));
            try {
                scanner.scanAndRegister(basePackages);
                registerBuiltinPayloads();
            } catch (Exception e) {
                log.error("Error during T645 protocol scanning", e);
                if (properties.isFailOnError()) {
                    throw new RuntimeException("T645 protocol scanning failed", e);
                }
            }
        };
    }

    /**
     * 注册 DL/T 645-2007 内置标准报文映射。
     *
     * <p>映射关系：
     * <ul>
     *   <li>0x11 / DI=00010000 → 读当前组合有功总电能请求</li>
     *   <li>0x91 / DI=00010000 → 读当前组合有功总电能应答</li>
     *   <li>0x13 / DI=C0320000 → 读通信地址请求</li>
     *   <li>0x93 / DI=C0320000 → 读通信地址应答</li>
     *   <li>0x08 / DI=BROADCAST_TIME → 广播校时</li>
     *   <li>0x00 / DI=NONE → 4G/NB 心跳上报</li>
     *   <li>0x80 / DI=NONE → 4G/NB 心跳应答</li>
     * </ul>
     */
    private void registerBuiltinPayloads() {
        T645PayloadRegistry.register(0x11, "00010000", T645ReadEnergyRequest.class);
        T645PayloadRegistry.register(0x91, "00010000", T645ReadEnergyResponse.class);
        T645PayloadRegistry.register(0x13, "C0320000", T645ReadAddressRequest.class);
        T645PayloadRegistry.register(0x93, "C0320000", T645ReadAddressResponse.class);
        T645PayloadRegistry.register(0x08, "BROADCAST_TIME", T645BroadcastTime.class);
        
        // 4G/NB 心跳
        T645PayloadRegistry.register(0x00, "NONE", T645HeartbeatRequest.class);
        T645PayloadRegistry.register(0x80, "NONE", T645HeartbeatResponse.class);
        
        log.info("Registered {} built-in T645 payloads", 7);
    }
}
