package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressFBWarnings("EI_EXPOSE_REP")
@Data
@ConfigurationProperties(prefix = "lambda.actuator")
public class ActuatorProperties {

    private Resource resource = new Resource();

    @Data
    public static class Resource {
        private String locationPattern = "classpath*:com/lambda/cloud/**/*.class";
    }
}
