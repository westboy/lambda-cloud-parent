package com.jingfang.autoconfig;

import com.jingfang.cloud.oss.client.OssClient;
import com.jingfang.cloud.oss.manager.OssClientManager;
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
            ossClientManager.set(config.getName(), new OssClient(config));
        }
        return ossClientManager;
    }
}
