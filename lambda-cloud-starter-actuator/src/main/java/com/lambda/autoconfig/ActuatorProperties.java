package com.lambda.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lambda.actuator")
public class ActuatorProperties {

    private Resource resource = new Resource();

    @Data
    public static class Resource {
        private String locationPattern = "classpath*:com/lambda/cloud/**/*.class";
    }
}
