package com.jingfang.cloud.core.utils;

import cn.dev33.satoken.stp.StpLogic;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.core.principal.LoginType;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

/**
 * OperatorUtils
 *
 * @author jpjoo
 */
@Slf4j
public class OperatorUtils {

    private static final LoginUser defaultUser = new LoginUser() {
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
        public Set<String> getRoles() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getPermissions() {
            return Collections.emptySet();
        }

        @Override
        public Boolean getAccountLocked() {
            return true;
        }

        @Override
        public Boolean getAccountExpired() {
            return true;
        }
    };

    public static LoginUser getOperator() {
        try {
            StpLogic stpLogic = LoginType.getActiveStpLogic();
            return getLoginUser(stpLogic);
        } catch (Exception e) {
            log.warn("获取用户失败，用户未登录！");
            return defaultUser;
        }
    }

    private static LoginUser getLoginUser(StpLogic userStpLogic) {
        return (LoginUser) userStpLogic.getTokenSession().get("loginUser");
    }

}
