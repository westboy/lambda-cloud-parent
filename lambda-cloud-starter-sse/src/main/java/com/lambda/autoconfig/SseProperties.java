package com.lambda.autoconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * SseProperties
 *
 * @author Jin
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "lambda.sse")
public class SseProperties {
    private long timeout = 30000L;
    private long heartbeatInterval = 15000L;
    private int maxRetryAttempts = 3;
    private boolean enableController = true;
    private boolean enableLoggingListener = true;
    private String endpointPrefix = "/sse";
    private String subscribePath = "/subscribe";
    private String sendPath = "/send";
    private String broadcastPath = "/broadcast";

}
