package com.lambda.autoconfig;

import com.lambda.cloud.oss.client.OssClient;
import com.lambda.cloud.oss.manager.OssClientManager;
import com.lambda.cloud.oss.upload.MultipartUploadStateManager;
import com.lambda.cloud.oss.upload.impl.InMemoryMultipartUploadStateManager;
import com.lambda.cloud.oss.upload.impl.RedisMultipartUploadStateManager;
import com.lambda.cloud.redis.helper.RedisHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS 自动配置类
 * 负责创建 OSS 客户端和相关依赖
 *
 * @author jpjoo
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OssProperties.class)
public class OssAutoConfiguration {

    @SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
    private OssProperties ossProperties;

    @Autowired
    public void setOssProperties(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    /**
     * 创建分片上传状态管理器
     * 优先使用 Redis 实现，如果 Redis 不可用则使用内存实现
     *
     * @param redisHelper Redis 辅助类（可选）
     * @return 分片上传状态管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public MultipartUploadStateManager multipartUploadStateManager(
            @Autowired(required = false) RedisHelper redisHelper) {
        if (redisHelper != null) {
            log.info("使用 Redis 实现的分片上传状态管理器");
            return new RedisMultipartUploadStateManager(redisHelper);
        } else {
            log.warn("Redis 不可用，使用内存实现的分片上传状态管理器（仅适用于单机环境）");
            return new InMemoryMultipartUploadStateManager();
        }
    }

    /**
     * 创建 OSS 客户端管理器
     * 根据配置初始化所有 OSS 客户端
     *
     * @param stateManager 分片上传状态管理器
     * @return OSS 客户端管理器
     */
    @Bean
    public OssClientManager getOssClientManager(MultipartUploadStateManager stateManager) {
        OssClientManager ossClientManager = new OssClientManager();

        log.info("开始初始化 OSS 客户端，配置数量: {}", ossProperties.getClients().size());

        for (OssProperties.Config config : ossProperties.getClients()) {
            try {
                OssClient ossClient = new OssClient(config);
                ossClient.setMultipartUploadStateManager(stateManager);
                ossClient.createBucket();

                // 使用新的 register 方法替代已废弃的 set 方法
                ossClientManager.register(config.getName(), ossClient);

                log.info(
                        "OSS 客户端初始化成功: name={}, type={}, endpoint={}",
                        config.getName(),
                        config.getType(),
                        config.getEndpoint());
            } catch (Exception e) {
                log.error("OSS 客户端初始化失败: name={}, error={}", config.getName(), e.getMessage(), e);
                throw new IllegalStateException("Failed to initialize OSS client: " + config.getName(), e);
            }
        }

        log.info("所有 OSS 客户端初始化完成，总数: {}", ossClientManager.size());
        return ossClientManager;
    }
}
