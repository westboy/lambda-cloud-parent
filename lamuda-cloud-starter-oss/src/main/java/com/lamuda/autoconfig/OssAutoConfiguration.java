package com.lamuda.autoconfig;

import com.lamuda.cloud.oss.client.OssClient;
import com.lamuda.cloud.oss.manager.OssClientManager;
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

    private OssProperties ossProperties;

    @Autowired
    public void setOssProperties(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Bean
    public OssClientManager getOssClientManager() {
        OssClientManager ossClientManager = new OssClientManager();
        for (OssProperties.Config config : ossProperties.getConfigs()) {
            OssClient ossClient = new OssClient(config);
            ossClient.createBucket();
            ossClientManager.set(config.getName(), ossClient);
        }
        return ossClientManager;
    }
}
