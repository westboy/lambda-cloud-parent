package com.lambda.autoconfig;

import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.*;
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

    @Getter
    @Setter
    public static class EncryptConfig {
        private Boolean enabled = false;
        private String key = "1234567890123456";
    }

    @Getter
    @Setter
    public static class TenantConfig {

        private final Set<String> DEFAULTS = Sets.newHashSet(
                "DUAL",
                "TABLES",
                "la_tenant",
                "la_tenant_datasource",
                "la_role_resources",
                "la_resources",
                "la_user_online_logs",
                "la_dict_type",
                "la_dict_info",
                "la_organization_roles",
                "la_configs",
                "la_config_options",
                "la_client_resources",
                "la_area",
                "la_api_resources",
                "la_api_token",
                "la_user_password_logs");

        private Boolean enabled = false;

        private String tenantColumn = "tenant_id";

        private Set<String> ignoreTables = new HashSet<>();

        public Set<String> getIgnoreTables() {
            return Sets.union(DEFAULTS, this.ignoreTables);
        }
    }
}
