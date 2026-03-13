package com.lambda.autoconfig;

import lombok.Data;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author w
 */
@Data
@ConfigurationProperties(prefix = "lambda.api-docs")
public class SwaggerProperties {

    String title;

    Boolean enabled = false;

    String docUri = "/swagger-ui.html";

    String version = "1.0.0";

    SpringDocConfigProperties.ApiDocs.OpenApiVersion openApiVersion =
            SpringDocConfigProperties.ApiDocs.OpenApiVersion.OPENAPI_3_1;

    Boolean tokenEnabled = true;

    String tokenName = "Authorization";

    String tokenSchemeName = "bearerAuth";

    String tokenBearerFormat = "JWT";

    String tokenScheme = "bearer";

    String tokenDescription = "在此输入 Bearer Token";
}
