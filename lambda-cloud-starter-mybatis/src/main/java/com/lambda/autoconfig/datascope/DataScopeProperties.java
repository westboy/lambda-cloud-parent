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
    private String organizationTableName = "ORGANIZATION";

    /**
     * 组织表别名
     */
    private String organizationTableAlias = "ORG";

    /**
     * 组织表ID字段名
     */
    private String organizationIdColumn = "ID";

    /**
     * 组织表父级ID路径字段名
     */
    private String organizationParentKeysColumn = "parentkeys";

    /**
     * 权限表名
     */
    private String dataScopeTableName = "DATASCOPES";

    /**
     * 权限表别名
     */
    private String dataScopeTableAlias = "DATASCOPE";

    /**
     * 权限表辅助别名（用于子查询或去重）
     */
    private String dataScopeTableAlias0 = "DATASCOPE0";

    /**
     * 权限表ID字段名
     */
    private String dataScopeIdColumn = "ID";

    /**
     * 权限表关联ID字段名
     */
    private String dataScopeTidColumn = "TID";

    /**
     * 权限表类型字段名
     */
    private String dataScopeTypeColumn = "DOMAIN_TYPE";

    /**
     * 权限表级别字段名
     */
    private String dataScopeRankColumn = "RANK_LEVEL";

    /**
     * 权限表选中状态字段名
     */
    private String dataScopeCheckedColumn = "CHECKED";

    /**
     * 数据视图表名前缀
     */
    private String dataViewTableNamePrefix = "V_DATAVIEW";

    /**
     * 数据视图表ID字段名
     */
    private String dataViewIdColumn = "ID";

    /**
     * 数据视图表SID字段名
     */
    private String dataViewSidColumn = "SID";

    /**
     * 超级管理员用户名列表，逗号分隔
     * 拥有这些用户名的用户将被视为数据拥有者（Owner），拥有全部权限
     */
    private List<String> superAdminUsernames = Collections.singletonList("admin");
}
