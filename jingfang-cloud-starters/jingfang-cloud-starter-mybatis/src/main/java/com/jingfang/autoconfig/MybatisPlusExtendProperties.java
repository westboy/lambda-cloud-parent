package com.jingfang.autoconfig;

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
@ConfigurationProperties(prefix = "mybatis-plus")
public class MybatisPlusExtendProperties {

    private String mapperPackage = "";

    private Map<String, String> databaseIdMap = new HashMap<>();

}