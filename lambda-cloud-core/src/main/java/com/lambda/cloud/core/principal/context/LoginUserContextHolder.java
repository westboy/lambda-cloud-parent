package com.lambda.cloud.core.principal.context;

import com.lambda.cloud.core.principal.context.strategy.ContextHolderStrategy;
import com.lambda.cloud.core.principal.context.strategy.ThreadLocalContextHolderStrategy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.util.Assert;

/**
 * SecurityContextHolder
 *
 * @author jpjoo
 */
@SuppressFBWarnings({"MS_EXPOSE_REP", "EI_EXPOSE_STATIC_REP2"})
public class LoginUserContextHolder {

    static {
        strategy = new ThreadLocalContextHolderStrategy();
    }

    private static ContextHolderStrategy strategy;

    public static void clearContext() {
        strategy.clearContext();
    }

    public static LoginUserContext getContext() {
        return strategy.getContext();
    }

    public static void setContext(LoginUserContext context) {
        strategy.setContext(context);
    }

    public static void setContextHolderStrategy(ContextHolderStrategy strategy) {
        Assert.notNull(strategy, "securityContextHolderStrategy cannot be null");
        LoginUserContextHolder.strategy = strategy;
    }

    public static ContextHolderStrategy getContextHolderStrategy() {
        return strategy;
    }

    public static LoginUserContext createEmptyContext() {
        return strategy.createEmptyContext();
    }
}
