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
 * StpLogicUtils
 *
 * @author jpjoo
 */
@Slf4j
public class StpLogicUtils {

    private static final Set<String> LOGIN_TYPE_SET = new HashSet<>();

    static {
        LOGIN_TYPE_SET.add(Constants.HMAC);
        LOGIN_TYPE_SET.add(StpUtil.getLoginType());
    }

    /**
     * 初始化
     *
     * @param loginTypeList 登陆类型列表
     */
    public static void initializeLoginTypes(Collection<String> loginTypeList) {
        StpLogicUtils.LOGIN_TYPE_SET.addAll(loginTypeList);
    }

    /**
     * 是否包含登陆类型
     *
     * @param type 登陆类型
     * @return boolean
     */
    public static boolean containsLoginType(String type) {
        if (type == null || type.isEmpty()) {
            return true;
        }
        return LOGIN_TYPE_SET.contains(type);
    }

    /**
     * 获取登陆类型
     *
     * @param type 登陆类型
     * @return StpLogic
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
     * 获取StpLogic
     *
     * @param loginType 登陆类型
     * @return StpLogic
     */
    public static StpLogic getStpLogic(String loginType) {
        if (loginType == null || loginType.isEmpty()) {
            return StpUtil.getStpLogic();
        }
        return getByLoginType(loginType);
    }

    /**
     * 获取Session
     *
     * @param accessToken 访问令牌
     * @return SaSession
     */
    public static SaSession getSaSession(String accessToken) {
        if (LOGIN_TYPE_SET.isEmpty()) {
            return StpUtil.getStpLogic().getTokenSessionByToken(accessToken);
        }
        for (String loginType : LOGIN_TYPE_SET) {
            SaSession saSession = getStpLogic(loginType).getTokenSessionByToken(accessToken);
            if (saSession != null) {
                return saSession;
            }
        }
        return null;
    }

    /**
     * 获取当前登陆类型
     *
     * @return StpLogic
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
}
