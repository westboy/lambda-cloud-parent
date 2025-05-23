package com.lambda.autoconfig;

import com.lambda.cloud.oss.client.OssClient;
import com.lambda.cloud.oss.manager.OssClientManager;
import com.lambda.cloud.redis.helper.RedisHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OssAutoConfiguration
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

    @Bean
    public OssClientManager getOssClientManager(@Autowired(required = false) RedisHelper redisHelper) {
        OssClientManager ossClientManager = new OssClientManager();
        for (OssProperties.Config config : ossProperties.getConfigs()) {
            OssClient ossClient = new OssClient(config);
            ossClient.setRedisHelper(redisHelper);
            ossClient.createBucket();
            ossClientManager.set(config.getName(), ossClient);
        }
        return ossClientManager;
    }
}
