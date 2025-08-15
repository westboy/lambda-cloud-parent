package com.lambda.cloud.core.principal;

import java.io.Serializable;
import java.security.Principal;

/**
 * 登录用户主体接口
 * <p>
 * 继承自 {@link Principal} 和 {@link Serializable}，定义了登录用户的基本信息结构。
 * 该接口用于在系统中表示已认证的用户，包含用户的身份信息、组织信息、租户信息
 * 以及账户状态等核心属性。
 *
 * <p>实现类应该提供用户的完整身份信息，用于权限控制、多租户管理等功能。
 *
 * <p>使用示例：
 * <pre>{@code
 * public class DefaultLoginUser implements LoginUser {
 *     private String username;
 *     private String credentials;
 *     private String orgId;
 *     private String tenantId;
 *     private Boolean accountLocked;
 *     private Boolean accountExpired;
 *
 *     // 实现接口方法...
 * }
 * }</pre>
 *
 * @author jpjoo
 * @since 1.0.0
 */
public interface LoginUser extends Principal, Serializable {

    /**
     * 获取用户凭证
     * <p>
     * 返回用户的认证凭证，如密码、令牌等。
     * 注意：在生产环境中应避免直接暴露敏感凭证信息。
     *
     * @return 用户凭证，可能为null（如基于令牌的认证）
     */
    String getCredentials();

    /**
     * 获取租户ID
     * <p>
     * 返回用户所属的租户标识，用于多租户系统中的数据隔离和权限控制。
     * 在SaaS应用中，不同租户的数据应该完全隔离。
     *
     * @return 租户ID，可能为null（如单租户系统）
     */
    String getTenantId();

    /**
     * 获取组织ID
     * <p>
     * 返回用户所属的组织标识，用于组织级别的权限控制和数据隔离。
     *
     * @return 组织ID，可能为null（如个人用户）
     */
    String getOrgId();

    /**
     * 获取账户锁定状态
     * <p>
     * 返回账户是否被锁定的状态。锁定的账户通常不能进行正常的业务操作。
     *
     * @return true表示账户已锁定，false表示账户正常，null表示状态未知
     */
    Boolean getAccountLocked();

    /**
     * 获取账户过期状态
     * <p>
     * 返回账户是否已过期的状态。过期的账户需要重新激活或续期。
     *
     * @return true表示账户已过期，false表示账户有效，null表示状态未知
     */
    Boolean getAccountExpired();
}
