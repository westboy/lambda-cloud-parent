package com.lambda.cloud.mybatis.tenant;

/**
 * 默认策略：无租户上下文时跳过租户拼接。
 *
 * <p>对应平台管理员跨租户查询 / 系统内部调用——这类主体应看到全部数据，跳过过滤即其语义。
 * 匿名用户在 web 层已被 {@code @SaCheckPermission} 拦截，不会到达这里。
 *
 * <p>下游若需在丢租户时直接拒绝（FAIL），注册自定义 {@link MissingTenantStrategy} bean 抛异常即可。
 *
 * @author Jin
 */
public class SkipOnMissingTenantStrategy implements MissingTenantStrategy {

    @Override
    public boolean skipOnMissingTenant(String tableName) {
        return true;
    }
}
