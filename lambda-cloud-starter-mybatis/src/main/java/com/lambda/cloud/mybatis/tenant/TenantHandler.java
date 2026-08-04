package com.lambda.cloud.mybatis.tenant;

import cn.hutool.core.util.StrUtil;
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

    private final MissingTenantStrategy missingTenantStrategy;

    public TenantHandler(MybatisPlusExtendProperties mybatisProperties) {
        this(mybatisProperties.getTenant(), new SkipOnMissingTenantStrategy());
    }

    @Override
    public Expression getTenantId() {
        String currentTenantId = TenantContextHolder.getCurrentTenantId();
        if (StrUtil.isEmpty(currentTenantId)) {
            return null;
        }
        return new StringValue(currentTenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return tenantConfig.getTenantColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 无租户上下文（平台管理员跨租户查询 / 系统内部调用）时交由 MissingTenantStrategy 决定，
        // 默认全表跳过；避免 getTenantId() 返回 null 拼出 tenant_id = NULL 恒假条件导致查空。
        if (MissingTenantStrategy.isMissingTenantContext()) {
            return missingTenantStrategy.skipOnMissingTenant(tableName);
        }
        return tenantConfig.getIgnoreTables().contains(tableName.toLowerCase());
    }
}
