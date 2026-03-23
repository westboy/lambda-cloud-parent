package com.lambda.autoconfig.datascope;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据权限配置属性
 *
 * @author Jin
 */
@Data
@SuppressFBWarnings("EI_EXPOSE_REP")
@ConfigurationProperties(prefix = "lambda.datascope")
public class DataScopeProperties {

    /**
     * 组织表名
     */
    private String organizationTableName = "la_organization";

    /**
     * 组织表别名
     */
    private String organizationTableAlias = "org";

    /**
     * 组织表ID字段名
     */
    private String organizationIdColumn = "id";

    /**
     * 组织表父级ID路径字段名
     */
    private String organizationParentKeysColumn = "parent_keys";

    /**
     * 权限表名
     */
    private String dataScopeTableName = "la_datascopes";

    /**
     * 权限表别名
     */
    private String dataScopeTableAlias = "datascope";

    /**
     * 权限表辅助别名（用于子查询或去重）
     */
    private String dataScopeTableAlias0 = "datascope0";

    /**
     * 权限表ID字段名
     */
    private String dataScopeIdColumn = "id";

    /**
     * 权限表关联ID字段名
     */
    private String dataScopeTidColumn = "tid";

    /**
     * 权限表主体类型字段名 (USER, ROLE, ORG 等)
     */
    private String dataScopeTargetTypeColumn = "target_type";

    /**
     * 权限表类型字段名
     */
    private String dataScopeTypeColumn = "domain_type";

    /**
     * 权限表级别字段名
     */
    private String dataScopeRankColumn = "rank_level";

    /**
     * 权限表选中状态字段名
     */
    private String dataScopeCheckedColumn = "checked";

    /**
     * 超级管理员的身份标识集合（如用户名 admin，或角色 ROLE_ADMIN），逗号分隔。
     * 拥有这些标识的主体将被视为数据拥有者（Owner），拥有全部权限，跳过数据权限拦截。
     */
    private List<String> superAdminIdentifiers = Collections.singletonList("admin");
}
