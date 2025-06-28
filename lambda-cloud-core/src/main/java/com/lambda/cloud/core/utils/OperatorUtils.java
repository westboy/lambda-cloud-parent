package com.lambda.cloud.core.utils;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.principal.LoginUser;
import lombok.extern.slf4j.Slf4j;

/**
 * OperatorUtils
 *
 * @author jpjoo
 */
@Slf4j
public class OperatorUtils {

    private static final LoginUser DEFAULT_USER = new LoginUser() {
        @Override
        public String getName() {
            return "guest";
        }

        @Override
        public String getUsername() {
            return "guest";
        }

        @Override
        public String getCredentials() {
            return "guest";
        }

        @Override
        public String getOrgId() {
            return "guest";
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

    public static LoginUser getOperator() {
        try {
            StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();
            return getLoginUser(stpLogic);
        } catch (Exception e) {
            log.warn("获取用户失败，用户未登录！");
            return DEFAULT_USER;
        }
    }

    public static LoginUser getLoginUser(StpLogic userStpLogic) {
        SaSession tokenSession = userStpLogic.getTokenSession();
        return (LoginUser) tokenSession.get("loginUser");
    }
}
