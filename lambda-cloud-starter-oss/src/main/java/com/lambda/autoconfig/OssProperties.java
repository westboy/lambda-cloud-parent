package com.lambda.autoconfig;

import com.lambda.cloud.oss.enums.OssType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Oss配置
 *
 * @author jpjoo
 */
@Data
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "springboot properties class")
@ConfigurationProperties(prefix = "lambda.oss")
public class OssProperties {

    @NestedConfigurationProperty
    private List<Config> clients = new ArrayList<>();

    @Data
    public static class Config {
        private String name;
        private String type = OssType.MINIO.name();
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String region;
        private Boolean isHttps = false;
        private String accessPolicy;

        @NestedConfigurationProperty
        private ClientConfig httpClientConfig = new ClientConfig();

        @Data
        public static class ClientConfig {
            private Integer connectionTimeout = 10 * 1000;
            private Integer socketTimeout = 50 * 1000;
            private Integer maxConnections = 50;
            private Integer requestTimeout = 0;
            private Integer clientExecutionTimeout = 0;
            private Long connectionTTL = -1L;
            private Long connectionMaxIdleMillis = 60 * 1000L;
        }
    }
}
