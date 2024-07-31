package com.jingfang.cloud.autoconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * MybatisPlusExtendProperties
 *
 * @author jpjoo
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "jingfang.mybatis")
public class MybatisPlusExtendProperties {

    private Map<String, String> databaseIdMap = new HashMap<>();

}