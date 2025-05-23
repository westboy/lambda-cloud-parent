package com.lambda.cloud.gateway.properties;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lambda.web.firewall")
public class GatewayFirewallProperties {

    private Boolean enabled = false;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP")
    private List<String> whites = new ArrayList<>();
}
