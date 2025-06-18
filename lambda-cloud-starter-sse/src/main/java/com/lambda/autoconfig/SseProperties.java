package com.lambda.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SseProperties
 *
 * @author Jin
 */
@Data
@ConfigurationProperties(prefix = "lambda.sse")
public class SseProperties {
    private long timeout = 30000L;
    private boolean enableController = true;
    private boolean enableLoggingListener = true;
    private String endpointPrefix = "/sse";
    private String subscribePath = "/subscribe";
    private String sendPath = "/send";
    private String broadcastPath = "/broadcast";

}
