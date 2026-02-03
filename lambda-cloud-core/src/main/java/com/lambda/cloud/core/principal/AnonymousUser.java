package com.lambda.cloud.core.principal;

/**
 * 默认游客用户实现。
 *
 * <p>当用户未登录或获取用户信息失败时使用该对象。
 * 该用户权限最小，所有敏感操作均被限制。</p>
 *
 * <h3>特征：</h3>
 * <ul>
 *   <li>用户名：anonymous</li>
 *   <li>账户状态：已锁定且已过期</li>
 *   <li>组织ID：anonymous</li>
 *   <li>租户ID：-1（表示无效租户）</li>
 * </ul>
 */
public class AnonymousUser implements LoginUser {
    @Override
    public String getName() {
        return "anonymous";
    }

    @Override
    public String getCredentials() {
        return null;
    }

    @Override
    public String getOrgId() {
        return null;
    }

    @Override
    public Boolean getAccountLocked() {
        return true;
    }

    @Override
    public Boolean getAccountExpired() {
        return true;
    }

    @Override
    public String getTenantId() {
        return "-1";
    }
}
