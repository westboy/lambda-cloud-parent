package com.lambda.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lambda.cloud.core.principal.LoginUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 简单登录用户实现类
 * <p>
 * 实现了Lambda Cloud核心模块的LoginUser接口，提供基础的用户认证信息。
 * 包含用户的基本信息、账户状态、角色权限等核心属性。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>存储用户基本信息（ID、用户名、密码）</li>
 *   <li>管理账户状态（过期、锁定状态）</li>
 *   <li>维护用户角色和权限集合</li>
 *   <li>提供认证凭据和身份标识</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>密码字段使用@JsonIgnore注解，避免序列化泄露</li>
 *   <li>支持账户过期和锁定状态检查</li>
 *   <li>角色和权限采用Set集合，避免重复</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * SimpleLoginUser user = new SimpleLoginUser();
 * user.setId("123");
 * user.setUsername("admin");
 * user.setRoles(Set.of("ADMIN", "USER"));
 * user.setPermissions(Set.of("READ", "WRITE"));
 * }</pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see LoginUser
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "springboot properties")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SimpleLoginUser implements LoginUser {

    /**
     * 用户唯一标识
     * <p>
     * 用户在系统中的唯一标识符，通常为数据库主键或UUID。
     * 用于在整个系统中唯一标识一个用户。
     * </p>
     */
    private String id;

    /**
     * 用户名
     * <p>
     * 用户登录时使用的用户名，通常为邮箱、手机号或自定义用户名。
     * 在系统中应保证唯一性。
     * </p>
     */
    private String username;

    /**
     * 用户密码
     * <p>
     * 用户的登录密码，通常经过加密处理存储。
     * 使用@JsonIgnore注解防止在JSON序列化时泄露密码信息。
     * </p>
     */
    @JsonIgnore
    private String password;

    /**
     * 账户是否过期
     * <p>
     * 标识用户账户是否已过期。
     * true表示账户已过期，false表示账户未过期。
     * 过期的账户无法进行登录操作。
     * </p>
     */
    private Boolean accountExpired;

    /**
     * 账户是否被锁定
     * <p>
     * 标识用户账户是否被锁定。
     * true表示账户已锁定，false表示账户未锁定。
     * 锁定的账户无法进行登录操作，通常因为多次登录失败或安全原因。
     * </p>
     */
    private Boolean accountLocked;

    /**
     * 用户角色集合
     * <p>
     * 用户拥有的角色列表，用于基于角色的访问控制(RBAC)。
     * 使用Set集合避免重复角色，默认为空集合。
     * 角色通常表示用户的职能或权限级别，如ADMIN、USER等。
     * </p>
     */
    private Set<String> roles = Set.of();

    /**
     * 用户权限集合
     * <p>
     * 用户拥有的具体权限列表，用于细粒度的权限控制。
     * 使用Set集合避免重复权限，默认为空集合。
     * 权限通常表示具体的操作权限，如READ、WRITE、DELETE等。
     * </p>
     */
    private Set<String> permissions = Set.of();

    /**
     * 获取用户认证凭据
     * <p>
     * 返回用户的认证凭据，通常为密码。
     * 使用@JsonIgnore注解防止在JSON序列化时泄露凭据信息。
     * </p>
     *
     * @return 用户密码作为认证凭据
     */
    @JsonIgnore
    @Override
    public String getCredentials() {
        return password;
    }

    /**
     * 获取用户所属组织ID
     * <p>
     * 返回用户所属的组织标识。
     * 在简单实现中返回空字符串，实际应用中可根据需要返回具体的组织ID。
     * </p>
     *
     * @return 组织ID，简单实现返回空字符串
     */
    @Override
    public String getOrgId() {
        return "";
    }

    /**
     * 获取用户所属租户ID
     * <p>
     * 返回用户所属的租户标识，用于多租户系统中的数据隔离。
     * 在简单实现中返回空字符串，实际应用中可根据需要返回具体的租户ID。
     * </p>
     *
     * @return 租户ID，简单实现返回空字符串
     */
    @Override
    public String getTenantId() {
        return "";
    }

    /**
     * 获取用户名称标识
     * <p>
     * 返回用户的名称标识，在此实现中返回用户ID。
     * 使用@JsonIgnore注解防止在JSON序列化时重复输出。
     * </p>
     *
     * @return 用户ID作为名称标识
     */
    @JsonIgnore
    @Override
    public String getName() {
        return id;
    }
}
