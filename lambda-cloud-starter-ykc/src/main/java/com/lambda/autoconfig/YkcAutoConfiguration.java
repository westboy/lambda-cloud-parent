package com.lambda.autoconfig;

import com.lambda.cloud.netty.protocol.scanner.ProtocolPayloadScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * Protocol Payload 自动配置类
 * <p>
 * 自动扫描并注册带有 @ProtocolPayload 注解的协议消息类
 * </p>
 *
 * @author Lambda
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(YkcProperties.class)
@RequiredArgsConstructor
public class YkcAutoConfiguration {

    private final YkcProperties properties;

    /**
     * 创建协议扫描器
     */
    @Bean
    public ProtocolPayloadScanner protocolPayloadScanner() {
        return new ProtocolPayloadScanner();
    }

    /**
     * 应用启动完成后执行扫描
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> protocolScannerListener(
            ProtocolPayloadScanner scanner) {
        return event -> {
            if (properties.isLazyInit()) {
                log.info("Protocol scanner lazy initialization is enabled, skipping auto-scan");
                return;
            }

            String[] basePackages = properties.getBasePackages();
            if (basePackages == null || basePackages.length == 0) {
                // 如果没有配置扫描路径，使用默认路径
                basePackages = new String[]{"com.lambda"};
                log.info("No base packages configured, using default: {}", String.join(", ", basePackages));
            }

            log.info("Starting protocol auto-scan with packages: {}", String.join(", ", basePackages));
            try {
                scanner.scanAndRegister(basePackages);
            } catch (Exception e) {
                log.error("Error during protocol scanning", e);
                if (properties.isFailOnError()) {
                    throw new RuntimeException("Protocol scanning failed", e);
                }
            }
        };
    }
}