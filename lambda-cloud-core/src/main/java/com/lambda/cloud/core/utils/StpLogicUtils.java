package com.lambda.cloud.core.utils;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.lambda.cloud.core.Constants;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Sa-Token登录逻辑工具类
 * <p>
 * 该工具类提供了对Sa-Token框架中多种登录类型的统一管理和操作功能。
 * 支持多种登录方式的并存，包括HMAC认证、普通用户登录等。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>管理多种登录类型的注册和查询</li>
 *   <li>根据登录类型获取对应的StpLogic实例</li>
 *   <li>提供会话管理和令牌验证功能</li>
 *   <li>自动检测当前活跃的登录状态</li>
 * </ul>
 *
 * <h3>支持的登录类型：</h3>
 * <ul>
 *   <li>HMAC认证 - 基于签名的API认证</li>
 *   <li>默认用户登录 - 标准的用户名密码登录</li>
 *   <li>自定义登录类型 - 通过初始化方法添加</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 初始化自定义登录类型
 * List<String> customTypes = Arrays.asList("admin", "api");
 * StpLogicUtils.initializeLoginTypes(customTypes);
 *
 * // 检查登录类型是否支持
 * boolean supported = StpLogicUtils.containsLoginType("hmac");
 *
 * // 获取特定类型的登录逻辑
 * StpLogic logic = StpLogicUtils.getStpLogic("hmac");
 *
 * // 获取当前活跃的登录逻辑
 * StpLogic activeLogic = StpLogicUtils.getActiveStpLogic();
 * }</pre>
 *
 * @author jpjoo
 * @see cn.dev33.satoken.stp.StpLogic
 * @see cn.dev33.satoken.stp.StpUtil
 * @see Constants
 */
@Slf4j
public class StpLogicUtils {

    /**
     * 登录类型集合
     * <p>
     * 存储系统支持的所有登录类型。默认包含HMAC认证和标准用户登录类型。
     * 可以通过initializeLoginTypes方法添加自定义登录类型。
     * </p>
     */
    private static final Set<String> LOGIN_TYPE_SET = new HashSet<>();

    static {
        LOGIN_TYPE_SET.add(Constants.HMAC);
        LOGIN_TYPE_SET.add(StpUtil.getLoginType());
    }

    /**
     * 初始化登录类型
     * <p>
     * 将自定义的登录类型添加到系统支持的登录类型集合中。
     * 该方法通常在应用启动时调用，用于注册额外的登录方式。
     * </p>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>登录类型不能为null或空字符串</li>
     *   <li>重复的登录类型会被自动去重</li>
     *   <li>建议在应用启动时一次性初始化所有类型</li>
     * </ul>
     *
     * @param loginTypeList 要添加的登录类型列表，不能为null
     */
    public static void initializeLoginTypes(Collection<String> loginTypeList) {
        StpLogicUtils.LOGIN_TYPE_SET.addAll(loginTypeList);
    }

    /**
     * 检查是否包含指定的登录类型
     * <p>
     * 判断给定的登录类型是否在系统支持的登录类型集合中。
     * 如果传入null或空字符串，则返回true（表示使用默认登录类型）。
     * </p>
     *
     * @param type 要检查的登录类型，可以为null或空字符串
     * @return 如果支持该登录类型返回true，否则返回false
     */
    public static boolean containsLoginType(String type) {
        if (type == null || type.isEmpty()) {
            return true;
        }
        return LOGIN_TYPE_SET.contains(type);
    }

    /**
     * 根据登录类型获取对应的StpLogic实例
     * <p>
     * 从已注册的登录类型中查找指定类型，并返回对应的Sa-Token登录逻辑实例。
     * 如果找不到对应的登录类型，会抛出异常。
     * </p>
     *
     * @param type 登录类型，必须是已注册的类型
     * @return 对应的StpLogic实例
     * @throws SaTokenException 如果登录类型不支持（错误码：CODE_10011）
     */
    public static StpLogic getByLoginType(String type) {
        return LOGIN_TYPE_SET.stream()
                .filter(loginType -> loginType.equals(type))
                .map(SaManager::getStpLogic)
                .findFirst()
                .orElseThrow(
                        (Supplier<RuntimeException>) () -> new SaTokenException(SaErrorCode.CODE_10011, "不支持的登陆类型"));
    }

    /**
     * 获取StpLogic实例
     * <p>
     * 根据登录类型获取对应的StpLogic实例。如果登录类型为null或空字符串，
     * 则返回默认的StpLogic实例。
     * </p>
     *
     * @param loginType 登录类型，可以为null或空字符串
     * @return StpLogic实例，永不为null
     * @throws SaTokenException 如果指定的登录类型不支持
     */
    public static StpLogic getStpLogic(String loginType) {
        if (loginType == null || loginType.isEmpty()) {
            return StpUtil.getStpLogic();
        }
        return getByLoginType(loginType);
    }

    /**
     * 根据访问令牌获取会话
     * <p>
     * 遍历所有已注册的登录类型，尝试根据访问令牌获取对应的会话信息。
     * 返回第一个找到的有效会话，如果所有登录类型都无法找到会话则返回null。
     * </p>
     *
     * <h3>查找策略：</h3>
     * <ul>
     *   <li>如果没有注册任何登录类型，使用默认StpLogic查找</li>
     *   <li>否则遍历所有登录类型，返回第一个匹配的会话</li>
     * </ul>
     *
     * @param accessToken 访问令牌，不能为null
     * @return 对应的会话对象，可能为null
     */
    public static SaSession getSaSession(String accessToken) {
        if (LOGIN_TYPE_SET.isEmpty()) {
            return StpUtil.getStpLogic().getTokenSessionByToken(accessToken, false);
        }
        for (String loginType : LOGIN_TYPE_SET) {
            SaSession saSession = getStpLogic(loginType).getTokenSessionByToken(accessToken, false);
            if (saSession != null) {
                return saSession;
            }
        }
        return null;
    }

    /**
     * 获取当前活跃的登录逻辑
     * <p>
     * 自动检测当前用户的登录状态，返回对应的StpLogic实例。
     * 该方法会遍历所有已注册的登录类型，找到第一个处于登录状态的逻辑。
     * </p>
     *
     * <h3>检测策略：</h3>
     * <ul>
     *   <li>如果没有注册登录类型，直接检查默认登录状态</li>
     *   <li>否则遍历所有登录类型，返回第一个已登录的逻辑</li>
     *   <li>如果所有类型都未登录，抛出异常</li>
     * </ul>
     *
     * <h3>异常处理：</h3>
     * <ul>
     *   <li>忽略"未登录"异常，继续检查下一个类型</li>
     *   <li>其他异常直接抛出</li>
     *   <li>所有类型都检查完毕仍未找到，抛出"不支持的登录类型"异常</li>
     * </ul>
     *
     * @return 当前活跃的StpLogic实例
     * @throws SaTokenException 如果没有找到任何活跃的登录状态
     */
    public static StpLogic getActiveStpLogic() {
        if (LOGIN_TYPE_SET.isEmpty()) {
            StpLogic stpLogic = getStpLogic(null);
            stpLogic.checkLogin();
            return stpLogic;
        } else {
            int attempt = 0;
            for (String loginType : LOGIN_TYPE_SET) {
                StpLogic stpLogic = getStpLogic(loginType);
                try {
                    if (stpLogic.isLogin()) {
                        return stpLogic;
                    }
                    stpLogic.checkLogin();
                } catch (SaTokenException exception) {
                    if (exception.getCode() != SaErrorCode.CODE_11012 || attempt >= LOGIN_TYPE_SET.size() - 1) {
                        throw exception;
                    }
                    attempt++;
                }
            }
        }
        throw new SaTokenException(SaErrorCode.CODE_10011, "不支持的登陆类型");
    }

    public static void logoutByTokenValue(String accessToken) {
        for (String loginType : LOGIN_TYPE_SET) {
            try {
                StpLogic stpLogic = getStpLogic(loginType);
                stpLogic.logoutByTokenValue(accessToken);
            } catch (SaTokenException ignored) {
            }
        }
    }
}
