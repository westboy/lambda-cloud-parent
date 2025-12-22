package com.lambda.cloud.mybatis.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.lambda.autoconfig.MybatisPlusExtendProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

@AllArgsConstructor
public class TenantHandler implements TenantLineHandler {

    @SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
    private MybatisPlusExtendProperties.TenantConfig tenantConfig;

    public TenantHandler(MybatisPlusExtendProperties mybatisProperties) {
        this.tenantConfig = mybatisProperties.getTenant();
    }

    @Override
    public Expression getTenantId() {
        return new StringValue(TenantContextHolder.getCurrentTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return tenantConfig.getTenantColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return tenantConfig.getIgnoreTables().contains(tableName);
    }
}
