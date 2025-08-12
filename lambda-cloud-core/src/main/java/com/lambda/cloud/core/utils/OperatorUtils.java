package com.lambda.cloud.core.utils;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作员工具类
 * <p>
 * 提供当前登录用户信息的获取和管理功能。该工具类基于Sa-Token框架，
 * 用于在业务逻辑中获取当前操作员的身份信息。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>获取当前登录用户信息</li>
 *   <li>提供默认游客用户作为降级处理</li>
 *   <li>支持多种登录逻辑的用户获取</li>
 *   <li>异常情况下的安全降级</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>用户未登录时返回受限的默认用户</li>
 *   <li>异常情况下不会抛出错误，而是返回安全的默认值</li>
 *   <li>默认用户具有最小权限，账户被锁定且已过期</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 获取当前操作员
 * LoginUser currentUser = OperatorUtils.getOperator();
 * String username = currentUser.getUsername();
 *
 * // 检查是否为游客用户
 * if ("guest".equals(currentUser.getUsername())) {
 *     // 处理未登录情况
 * }
 *
 * // 基于特定登录逻辑获取用户
 * StpLogic customLogic = StpUtil.stpLogic;
 * LoginUser user = OperatorUtils.getLoginUser(customLogic);
 * }</pre>
 *
 * @author jpjoo
 * @see LoginUser
 * @see cn.dev33.satoken.stp.StpLogic
 * @see StpLogicUtils
 */
@Slf4j
public class OperatorUtils {

    /**
     * 默认游客用户
     * <p>
     * 当用户未登录或获取用户信息失败时返回的默认用户对象。
     * 该用户具有最小权限，所有敏感操作都被限制。
     * </p>
     *
     * <h3>默认用户特征：</h3>
     * <ul>
     *   <li>用户名：guest</li>
     *   <li>账户状态：已锁定且已过期</li>
     *   <li>租户ID：-1（表示无效租户）</li>
     *   <li>组织ID：guest</li>
     * </ul>
     */
    private static final LoginUser DEFAULT_USER = new LoginUser() {
        @Override
        public String getName() {
            return "anonymous";
        }

        @Override
        public String getUsername() {
            return "anonymous";
        }

        @Override
        public String getCredentials() {
            return "anonymous";
        }

        @Override
        public String getOrgId() {
            return "anonymous";
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
    };

    /**
     * 获取当前操作员信息
     * <p>
     * 获取当前登录用户的详细信息。该方法会自动处理异常情况，
     * 当用户未登录或获取失败时，返回安全的默认游客用户。
     * </p>
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li>获取当前活跃的登录逻辑</li>
     *   <li>从会话中提取用户信息</li>
     *   <li>异常时返回默认游客用户</li>
     * </ol>
     *
     * <h3>异常处理：</h3>
     * <ul>
     *   <li>用户未登录：返回DEFAULT_USER</li>
     *   <li>会话过期：返回DEFAULT_USER</li>
     *   <li>其他异常：记录警告日志并返回DEFAULT_USER</li>
     * </ul>
     *
     * @return 当前登录用户信息，永不为null
     */
    public static LoginUser getOperator() {
        try {
            StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();
            return getLoginUser(stpLogic);
        } catch (Exception e) {
            log.warn("获取用户失败，用户未登录！");
            return DEFAULT_USER;
        }
    }

    /**
     * 基于指定登录逻辑获取用户信息
     * <p>
     * 从指定的Sa-Token登录逻辑中获取当前登录用户的信息。
     * 该方法直接从Token会话中提取用户对象，不进行异常处理。
     * </p>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>调用前需确保用户已登录</li>
     *   <li>会话中必须存在"loginUser"键</li>
     *   <li>可能抛出运行时异常，需要调用方处理</li>
     * </ul>
     *
     * @param userStpLogic Sa-Token登录逻辑实例，不能为null
     * @return 登录用户信息，可能为null
     * @throws RuntimeException 如果获取会话失败或用户未登录
     */
    public static LoginUser getLoginUser(StpLogic userStpLogic) {
        LoginUser loginUser = (LoginUser) SaHolder.getStorage().get(Constants.LOGIN_USER);
        if (loginUser != null) {
            return loginUser;
        }
        SaSession session = StpUtil.getTokenSession();
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        SaSession tokenSession = userStpLogic.getTokenSession();
        loginUser = (LoginUser) tokenSession.get(Constants.LOGIN_USER);
        SaHolder.getStorage().set(Constants.LOGIN_USER, loginUser);
        return loginUser;
    }

    /**
     * 获取当前登录用户
     * @param clazz 期望的用户类型
     * @param <T> LoginUser的子类型
     * @return 指定类型的登录用户实例
     * @throws IllegalStateException 当用户未登录时
     * @throws ClassCastException 当类型转换失败时
     */
    public static <T extends LoginUser> T getLoginUser(Class<T> clazz) {
        try {
            LoginUser loginUser = getOperator();
            if (loginUser == null) {
                throw new IllegalStateException("User not logged in");
            }
            if (!clazz.isInstance(loginUser)) {
                throw new ClassCastException(String.format(
                        "Cannot cast %s to %s", loginUser.getClass().getSimpleName(), clazz.getSimpleName()));
            }
            return clazz.cast(loginUser);
        } catch (Exception e) {
            log.error("Failed to get login user of type {}: {}", clazz.getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
