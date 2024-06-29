package com.jingfang.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO配置
 *
 * @author jpjoo
 */
@Data
@ConfigurationProperties(prefix = "calis.minio")
public class MinioProperties {

    /**
     * 服务器地址
     */
    private String endpoint;

    /**
     * 用户名
     */
    private String accessKey;

    /**
     * 密码
     */
    private String accessSecret;

    /**
     * 存储桶
     */
    private String bucket;
}
