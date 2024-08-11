package com.jingfang.security.utils;

import cn.dev33.satoken.stp.StpLogic;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.security.context.SecurityContext;
import com.jingfang.security.context.SecurityContextHolder;
import com.jingfang.security.enums.LoginType;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * OperatorUtils
 *
 * @author jpjoo
 */
public class OperatorUtils {

    public static LoginUser getOperator() {
        StpLogic stpLogic = LoginType.getActiveStpLogic();
        return getLoginUser(stpLogic);
    }

    private static LoginUser getLoginUser(StpLogic userStpLogic) {
        return userStpLogic.getTokenSession().get("loginUser", new LoginUser() {
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
        });
    }

}
