package com.lambda.cloud.gateway.properties;

import com.google.common.collect.Lists;
import com.lambda.cloud.core.model.KeyValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "springboot properties")
@Data
@ConfigurationProperties(prefix = "lambda.web.firewall")
public class GatewayFirewallProperties {

    private Boolean enabled = false;

    private List<String> whites = new ArrayList<>();

    private List<KeyValue> loginTypes = Lists.newArrayList();
}
