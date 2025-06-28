package com.lambda.cloud.core.principal.context.strategy;

import com.lambda.cloud.core.principal.context.LoginUserContext;
import com.lambda.cloud.core.principal.context.LoginUserContextImpl;
import com.lambda.cloud.core.utils.Assert;

/**
 * InheritableThreadLocalSecurityContextHolderStrategy
 * 子线程支持
 *
 * @author Jin
 */
public final class InheritableThreadLocalContextHolderStrategy implements ContextHolderStrategy {

    private static final ThreadLocal<LoginUserContext> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    @Override
    public void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    @Override
    public LoginUserContext getContext() {
        LoginUserContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    @Override
    public void setContext(LoginUserContext context) {
        Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
        CONTEXT_HOLDER.set(context);
    }

    @Override
    public LoginUserContext createEmptyContext() {
        return new LoginUserContextImpl();
    }
}
