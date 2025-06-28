package com.lambda.cloud.core.principal.context.strategy;

import com.lambda.cloud.core.principal.context.LoginUserContext;
import com.lambda.cloud.core.principal.context.LoginUserContextImpl;
import com.lambda.cloud.core.utils.Assert;

/**
 * ThreadLocalSecurityContextHolderStrategy
 *
 * @author jpjoo
 */
public final class ThreadLocalContextHolderStrategy implements ContextHolderStrategy {
    private static final ThreadLocal<LoginUserContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    public LoginUserContext getContext() {
        LoginUserContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = this.createEmptyContext();
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    public void setContext(LoginUserContext context) {
        Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
        CONTEXT_HOLDER.set(context);
    }

    public LoginUserContext createEmptyContext() {
        return new LoginUserContextImpl();
    }
}
