package com.lambda.cloud.core.utils;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import lombok.extern.slf4j.Slf4j;

import static com.lambda.cloud.core.Constants.ANONYMOUS_USER;

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
     * 获取当前登录用户信息。
     *
     * <p>直接从当前活跃的 StpLogic 会话中提取用户信息。
     * 如果用户未登录或会话异常，将抛出异常，由调用方处理。</p>
     *
     * <h3>执行流程：</h3>
     * <ol>
     *     <li>获取当前活跃的登录逻辑（StpLogic）。</li>
     *     <li>从会话中提取 LoginUser 对象。</li>
     *     <li>如果未登录或获取失败，抛出异常。</li>
     * </ol>
     *
     * @return 当前登录用户信息
     * @throws NotLoginException 用户未登录或会话失效
     */
    public static LoginUser getOperator() {
        try {
            StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();
            return getLoginUser(stpLogic);
        } catch (Exception e) {
            log.error("获取用户失败，用户未登录！",e);
            throw e;
        }
    }

    /**
     * 安全获取当前登录用户信息。
     *
     * <p>从当前活跃 StpLogic 会话中提取用户信息，
     * 当用户未登录或获取失败时，返回默认游客用户</p>
     *
     * <h3>执行流程：</h3>
     * <ol>
     *     <li>获取当前活跃的登录逻辑（StpLogic）。</li>
     *     <li>尝试从会话中获取 LoginUser 对象。</li>
     *     <li>异常时记录日志并返回默认用户。</li>
     * </ol>
     *
     * <h3>异常处理：</h3>
     * <ul>
     *     <li>用户未登录或会话过期：返回 ANONYMOUS_USER 并记录 warn 日志。</li>
     *     <li>其他异常：记录 error 日志并返回 ANONYMOUS_USER。</li>
     * </ul>
     *
     * @return 当前登录用户信息，永不为 null
     */
    public static LoginUser getSafeOperator() {
        try {
            StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();
            return getLoginUser(stpLogic);
        } catch (NotLoginException e) {
            log.warn("获取用户失败，返回默认用户", e);
            return ANONYMOUS_USER;
        } catch (Exception e) {
            log.error("获取用户出现未知异常", e);
            return ANONYMOUS_USER;
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
        SaSession tokenSession = userStpLogic.getTokenSession();
        loginUser = (LoginUser) tokenSession.get(Constants.LOGIN_USER);
        SaHolder.getStorage().set(Constants.LOGIN_USER, loginUser);
        return loginUser;
    }

    /**
     * 获取当前登录用户
     *
     * @param clazz 期望的用户类型
     * @param <T>   LoginUser 的子类型
     * @return 指定类型的登录用户实例
     * @throws IllegalStateException 当用户未登录时
     * @throws ClassCastException    当类型转换失败时
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
