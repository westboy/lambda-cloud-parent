package com.jingfang.security.utils;

import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.security.context.SecurityContext;
import com.jingfang.security.context.SecurityContextHolder;
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
        return getOperatorOrDefault(() -> new LoginUser() {
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

    public static LoginUser getOperatorOrDefault(@NotNull Supplier<LoginUser> defaultOperator) {
        SecurityContext context = SecurityContextHolder.getContext();
        try {
            return Objects.requireNonNull(context.getPrincipal());
        } catch (Exception e) {
            return defaultOperator.get();
        }
    }

}
