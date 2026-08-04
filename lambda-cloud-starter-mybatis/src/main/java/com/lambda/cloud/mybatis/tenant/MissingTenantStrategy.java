package com.lambda.cloud.mybatis.tenant;

import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.core.exception.IllegalStateException;

/**
 * 无租户上下文时的处理策略。
 *
 * <p>{@link TenantContextHolder} 当前租户为空（平台管理员跨租户查询 / 系统内部调用）时，
 * 由本策略决定 {@link TenantHandler#ignoreTable(String)} 是否跳过该表的租户拼接。
 *
 * <p>实现只允许二选一：返回 {@code true} 跳过租户拼接，或抛 {@link IllegalStateException} 拒绝执行。
 * 不允许「不跳过且 getTenantId() 返回 null」，那会拼出 {@code tenant_id = NULL} 恒假条件。
 *
 * <p>默认实现 {@link SkipOnMissingTenantStrategy}（{@code @ConditionalOnMissingBean}）。
 * 下游注册自己的 bean 即可改变行为；需彻底自定义则整体替换 {@code TenantLineHandler} bean。
 *
 * @author Jin
 */
@FunctionalInterface
public interface MissingTenantStrategy {

    /**
     * 无租户上下文时，是否跳过指定表的租户拼接。
     *
     * @param tableName 当前 SQL 涉及的表名
     * @return {@code true} 跳过租户拼接
     * @throws IllegalStateException 拒绝执行时抛出
     */
    boolean skipOnMissingTenant(String tableName);

    /**
     * 当前是否无租户上下文。
     */
    static boolean isMissingTenantContext() {
        return StrUtil.isEmpty(TenantContextHolder.getCurrentTenantId());
    }
}
