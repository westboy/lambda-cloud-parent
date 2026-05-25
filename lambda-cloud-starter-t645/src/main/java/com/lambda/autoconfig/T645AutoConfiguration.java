package com.lambda.autoconfig;

import com.lambda.cloud.netty.protocol.scanner.ProtocolPayloadScanner;
import com.lambda.cloud.t645.message.T645PayloadRegistry;
import com.lambda.cloud.t645.message.control.T645BroadcastTime;
import com.lambda.cloud.t645.message.read.T645ReadAddressRequest;
import com.lambda.cloud.t645.message.read.T645ReadAddressResponse;
import com.lambda.cloud.t645.message.read.T645ReadEnergyRequest;
import com.lambda.cloud.t645.message.read.T645ReadEnergyResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SuppressFBWarnings("EI_EXPOSE_REP")
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(T645Properties.class)
@RequiredArgsConstructor
public class T645AutoConfiguration {

    private final T645Properties properties;

    @Bean
    public ProtocolPayloadScanner t645ProtocolPayloadScanner() {
        return new ProtocolPayloadScanner();
    }

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

    private void registerBuiltinPayloads() {
        T645PayloadRegistry.register(0x11, "00010000", T645ReadEnergyRequest.class);
        T645PayloadRegistry.register(0x91, "00010000", T645ReadEnergyResponse.class);
        T645PayloadRegistry.register(0x13, "C0320000", T645ReadAddressRequest.class);
        T645PayloadRegistry.register(0x93, "C0320000", T645ReadAddressResponse.class);
        T645PayloadRegistry.register(0x08, "BROADCAST_TIME", T645BroadcastTime.class);
        log.info("Registered {} built-in T645 payloads", 5);
    }
}
