package com.lambda.cloud.gateway.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "lambda.web.firewall")
public class GatewayFirewallProperties {

    private Boolean enabled = false;

    private List<String> whites = new ArrayList<>();

}
