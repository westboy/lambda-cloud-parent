package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Lambda Dubbo增强配置属性
 * <p>
 * 提供dubbo-spring-boot-starter之外的企业级增强功能配置，
 * 包括安全认证、监控可观测性、重试机制、多租户支持等功能。
 * </p>
 *
 * <p>配置前缀: lambda.dubbo</p>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "lambda.dubbo")
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "Spring Boot Configuration Properties require mutable objects for binding")
public class DubboProperties {

    /**
     * 安全认证配置
     */
    private Security security = new Security();

    /**
     * 监控和可观测性配置
     */
    private Monitoring monitoring = new Monitoring();

    /**
     * 重试机制配置
     */
    private Retry retry = new Retry();

    /**
     * 多租户配置
     */
    private Tenant tenant = new Tenant();

    /**
     * 安全认证配置类
     * <p>用于配置Dubbo服务调用的安全认证相关参数</p>
     */
    @Data
    public static class Security {
        /**
         * 是否启用安全认证功能
         * <p>默认: true</p>
         */
        private boolean enabled = true;

        /**
         * 认证令牌请求头名称
         * <p>默认: Authorization</p>
         */
        private String tokenHeader = "Authorization";

        /**
         * 用户ID请求头名称
         * <p>默认: X-User-Id</p>
         */
        private String userHeader = "X-User-Id";

        /**
         * 租户ID请求头名称
         * <p>默认: X-Tenant-Id</p>
         */
        private String tenantHeader = "X-Tenant-Id";
    }

    /**
     * 监控和可观测性配置类
     * <p>用于配置性能监控和日志记录等功能</p>
     */
    @Data
    public static class Monitoring {
        /**
         * 是否启用监控功能
         * <p>默认: true</p>
         */
        private boolean enabled = true;

        /**
         * 是否启用性能指标收集
         * <p>默认: true</p>
         */
        private boolean enableMetrics = true;

        /**
         * 是否启用请求日志记录
         * <p>默认: true</p>
         */
        private boolean enableLogging = true;

        /**
         * 慢调用阈值(毫秒)
         * <p>超过此阈值的调用会被标记为慢调用并记录警告日志</p>
         * <p>默认: 1000ms</p>
         */
        private long slowCallThreshold = 1000L;
    }

    /**
     * 重试机制配置类
     * <p>用于配置Dubbo服务调用失败时的重试策略</p>
     */
    @Data
    public static class Retry {
        /**
         * 是否启用重试机制
         * <p>默认: true</p>
         */
        private boolean enabled = true;

        /**
         * 最大重试次数
         * <p>包含首次调用，默认: 3</p>
         */
        private int maxAttempts = 3;

        /**
         * 初始重试间隔(毫秒)
         * <p>默认: 1000ms</p>
         */
        private long initialInterval = 1000L;

        /**
         * 重试间隔倍数
         * <p>每次重试间隔 = 上次间隔 * multiplier，默认: 2.0</p>
         */
        private double multiplier = 2.0;

        /**
         * 最大重试间隔(毫秒)
         * <p>重试间隔的上限，默认: 10000ms</p>
         */
        private long maxInterval = 10000L;

        /**
         * 可重试的异常类型列表
         * <p>只有这些异常类型才会触发重试机制</p>
         */
        private List<String> retryableExceptions =
                List.of("java.util.concurrent.TimeoutException", "java.net.SocketTimeoutException");
    }

    /**
     * 多租户配置类
     * <p>用于配置多租户隔离和上下文传播</p>
     */
    @Data
    public static class Tenant {
        /**
         * 是否启用多租户功能
         * <p>默认: false</p>
         */
        private boolean enabled = false;

        /**
         * 租户ID请求头名称
         * <p>默认: X-Tenant-Id</p>
         */
        private String tenantIdHeader = "X-Tenant-Id";

        /**
         * 默认租户ID
         * <p>当请求中没有租户信息时使用，默认: default</p>
         */
        private String defaultTenant = "default";

        /**
         * 是否继承租户上下文
         * <p>是否自动传播当前线程的租户上下文到下游服务，默认: true</p>
         */
        private boolean inheritTenantContext = true;
    }
}
