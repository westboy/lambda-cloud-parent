package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressFBWarnings("EI_EXPOSE_REP")
@Data
@ConfigurationProperties(prefix = "lambda.t645.protocol")
public class T645Properties {

    private String[] basePackages = {"com.lambda.cloud.t645.message"};

    private boolean enabled = true;

    private boolean lazyInit = false;

    private boolean failOnError = false;
}
