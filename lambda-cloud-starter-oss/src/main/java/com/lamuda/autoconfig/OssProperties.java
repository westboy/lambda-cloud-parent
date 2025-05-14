package com.lambda.autoconfig;

import com.lambda.cloud.oss.enums.OssType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Oss配置
 *
 * @author jpjoo
 */
@Data
@ConfigurationProperties(prefix = "lambda.oss")
public class OssProperties {

    private List<Config> configs = new ArrayList<>();

    @Data
    public static class Config {
        private String name;
        private String type = OssType.MINIO.name();
        private String endpoint;
        private String accessKey ;
        private String secretKey;
        private String bucket;
        private String region;
        private Boolean isHttps = false;
        private String accessPolicy;
    }
}
