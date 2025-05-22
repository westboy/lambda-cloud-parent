package com.lambda.autoconfig;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * MybatisPlusExtendProperties
 *
 * @author jpjoo
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "mybatis-plus")
public class MybatisPlusExtendProperties {

    private String mapperPackage = "com.lambda.cloud.**.mapper";

    @NestedConfigurationProperty
    private Encrypt encrypt = new Encrypt();

    private Map<String, String> databaseIdMap = new HashMap<>();

    @Data
    public static class Encrypt {
        private String key = "1234567890123456";
        private Boolean enabled = false;
    }
}
