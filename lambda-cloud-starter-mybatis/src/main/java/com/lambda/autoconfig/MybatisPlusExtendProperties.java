package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * MybatisPlusExtendProperties
 *
 * @author jpjoo
 */
@Setter
@Getter
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "springboot properties class")
@ConfigurationProperties(prefix = "mybatis-plus")
public class MybatisPlusExtendProperties {

    private String mapperPackage;

    @NestedConfigurationProperty
    private EncryptConfig encrypt = new EncryptConfig();

    @NestedConfigurationProperty
    private Map<String, String> databaseIdMap = new HashMap<>();

    @NestedConfigurationProperty
    private TenantConfig tenant = new TenantConfig();

    @Data
    public static class EncryptConfig {
        private Boolean enabled = false;
        private String key = "1234567890123456";
    }

    @Data
    public static class TenantConfig {
        private Boolean enabled = false;
        private String tenantColumn = "tenant_id";
        private List<String> ignoreTables = new ArrayList<>();
    }
}
