package com.lambda.autoconfig;

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
@ConfigurationProperties(prefix = "lambda.purview")
public class PurviewProperties {

    /**
     * 组织表名
     */
    private String organizationTableName = "ORGANIZATION";

    /**
     * 组织表别名
     */
    private String organizationTableAlias = "ORGA";

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
    private String purviewTableName = "PURVIEWS";

    /**
     * 权限表别名
     */
    private String purviewTableAlias = "PURV";

    /**
     * 权限表辅助别名（用于子查询或去重）
     */
    private String purviewTableAlias0 = "PURV0";

    /**
     * 权限表ID字段名
     */
    private String purviewIdColumn = "ID";

    /**
     * 权限表关联ID字段名
     */
    private String purviewTidColumn = "TID";

    /**
     * 权限表类型字段名
     */
    private String purviewTypeColumn = "type2";

    /**
     * 权限表级别字段名
     */
    private String purviewRankColumn = "rank2";

    /**
     * 权限表选中状态字段名
     */
    private String purviewCheckedColumn = "checked";

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
    private String dataViewSidColumn = "sid";

    /**
     * 超级管理员用户名列表，逗号分隔
     * 拥有这些用户名的用户将被视为数据拥有者（Owner），拥有全部权限
     */
    private List<String> superAdminUsernames = Collections.singletonList("admin");
}
