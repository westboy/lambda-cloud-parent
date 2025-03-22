package com.lamuda.cloud.swagger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author w
 */
@Data
@ConfigurationProperties(prefix = "lamuda.api-docs")
public class SwaggerProperties {

    String title;

    Boolean enabled;
    String  docUri = "/swagger-ui.html";

    String version = "1.0.0";
}
