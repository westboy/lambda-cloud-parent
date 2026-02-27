package com.lambda.autoconfig;

import com.lambda.cloud.oss.enums.OssType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * OSS 配置属性
 * 支持多个 OSS 客户端配置
 *
 * @author jpjoo
 */
@Data
@Validated
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "springboot properties class")
@ConfigurationProperties(prefix = "lambda.oss")
public class OssProperties {

    /**
     * OSS 客户端配置列表
     */
    @Valid
    @NestedConfigurationProperty
    private List<Config> clients = new ArrayList<>();

    /**
     * 单个 OSS 客户端配置
     */
    @Data
    @Validated
    public static class Config {

        /**
         * 客户端名称（唯一标识）
         */
        @NotBlank(message = "OSS 客户端名称不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "OSS 客户端名称只能包含字母、数字、下划线和连字符")
        private String name;

        /**
         * OSS 类型（MINIO、ALIYUN、QCLOUD、QINIU、OTHER）
         */
        @NotBlank(message = "OSS 类型不能为空")
        @Pattern(
                regexp = "^(MINIO|ALIYUN|QCLOUD|QINIU|OTHER)$",
                message = "OSS 类型必须是 MINIO、ALIYUN、QCLOUD、QINIU 或 OTHER")
        private String type = OssType.MINIO.name();

        /**
         * OSS 服务端点
         */
        @NotBlank(message = "OSS 端点不能为空")
        private String endpoint;

        /**
         * 访问密钥 ID
         */
        @NotBlank(message = "AccessKey 不能为空")
        private String accessKey;

        /**
         * 访问密钥
         */
        @NotBlank(message = "SecretKey 不能为空")
        private String secretKey;

        /**
         * 存储桶名称
         */
        @NotBlank(message = "存储桶名称不能为空")
        @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$", message = "存储桶名称必须符合 DNS 命名规范（3-63个字符，小写字母、数字、连字符）")
        private String bucket;

        /**
         * 区域
         */
        private String region;

        /**
         * 是否使用 HTTPS
         */
        @NotNull(message = "isHttps 不能为空")
        private Boolean isHttps = false;

        /**
         * 访问策略
         */
        private String accessPolicy;

        /**
         *  AWS S3 的 path-style 和 bucket virtual hosting 两种访问方式
         */
        private Boolean enablePathStyleAccess;

        /**
         * HTTP 客户端配置
         */
        @Valid
        @NotNull(message = "HTTP 客户端配置不能为空")
        @NestedConfigurationProperty
        private ClientConfig httpClientConfig = new ClientConfig();

        /**
         * HTTP 客户端配置
         */
        @Data
        @Validated
        public static class ClientConfig {

            /**
             * 连接超时时间（毫秒）
             */
            @NotNull(message = "连接超时时间不能为空")
            @Min(value = 1000, message = "连接超时时间不能小于 1000 毫秒")
            private Integer connectionTimeout = 10 * 1000;

            /**
             * Socket 超时时间（毫秒）
             */
            @NotNull(message = "Socket 超时时间不能为空")
            @Min(value = 1000, message = "Socket 超时时间不能小于 1000 毫秒")
            private Integer socketTimeout = 50 * 1000;

            /**
             * 最大连接数
             */
            @NotNull(message = "最大连接数不能为空")
            @Min(value = 1, message = "最大连接数不能小于 1")
            private Integer maxConnections = 50;

            /**
             * 请求超时时间（毫秒，0 表示无限制）
             */
            @NotNull(message = "请求超时时间不能为空")
            @Min(value = 0, message = "请求超时时间不能小于 0")
            private Integer requestTimeout = 0;

            /**
             * 客户端执行超时时间（毫秒，0 表示无限制）
             */
            @NotNull(message = "客户端执行超时时间不能为空")
            @Min(value = 0, message = "客户端执行超时时间不能小于 0")
            private Integer clientExecutionTimeout = 0;

            /**
             * 连接 TTL（毫秒，-1 表示无限制）
             */
            @NotNull(message = "连接 TTL 不能为空")
            @Min(value = -1, message = "连接 TTL 不能小于 -1")
            private Long connectionTTL = -1L;

            /**
             * 连接最大空闲时间（毫秒）
             */
            @NotNull(message = "连接最大空闲时间不能为空")
            @Min(value = 1000, message = "连接最大空闲时间不能小于 1000 毫秒")
            private Long connectionMaxIdleMillis = 60 * 1000L;
        }
    }
}
