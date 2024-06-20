package com.jingfang.security.utils;

import com.jingfang.cloud.core.principal.Principal;
import com.jingfang.security.context.SecurityContext;
import com.jingfang.security.context.SecurityContextHolder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
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

    public static Principal getOperator() {
        return getOperatorOrDefault(() -> new Principal() {
            @Override
            public Serializable getUsername() {
                return "default";
            }

            @Override
            public String getCredentials() {
                return "default";
            }

            @Override
            public Set<Serializable> getRoles() {
                return Collections.emptySet();
            }

            @Override
            public Set<Serializable> getPermissions() {
                return Collections.emptySet();
            }
        });
    }

    public static Principal getOperatorOrDefault(@NotNull Supplier<Principal> defaultOperator) {
        SecurityContext context = SecurityContextHolder.getContext();
        try {
            return Objects.requireNonNull(context.getPrincipal());
        } catch (Exception e) {
            return defaultOperator.get();
        }
    }

}
