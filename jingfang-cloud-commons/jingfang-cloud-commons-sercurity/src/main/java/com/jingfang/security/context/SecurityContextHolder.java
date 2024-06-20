
package com.jingfang.security.context;

import com.jingfang.security.context.strategy.SecurityContextHolderStrategy;
import com.jingfang.security.context.strategy.ThreadLocalSecurityContextHolderStrategy;
import org.springframework.util.Assert;

/**
 * SecurityContextHolder
 *
 * @author jpjoo
 */
public class SecurityContextHolder {

    static {
        strategy = new ThreadLocalSecurityContextHolderStrategy();
    }

    private static SecurityContextHolderStrategy strategy;

    public static void clearContext() {
        strategy.clearContext();
    }

    public static SecurityContext getContext() {
        return strategy.getContext();
    }

    public static void setContext(SecurityContext context) {
        strategy.setContext(context);
    }

    public static void setContextHolderStrategy(SecurityContextHolderStrategy strategy) {
        Assert.notNull(strategy, "securityContextHolderStrategy cannot be null");
        SecurityContextHolder.strategy = strategy;
    }


    public static SecurityContextHolderStrategy getContextHolderStrategy() {
        return strategy;
    }


    public static SecurityContext createEmptyContext() {
        return strategy.createEmptyContext();
    }


}
