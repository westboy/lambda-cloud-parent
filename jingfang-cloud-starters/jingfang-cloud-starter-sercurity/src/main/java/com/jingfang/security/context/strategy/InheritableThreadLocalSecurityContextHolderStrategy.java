package com.jingfang.security.context.strategy;

import com.jingfang.cloud.core.utils.Assert;
import com.jingfang.security.context.SecurityContext;
import com.jingfang.security.context.SecurityContextImpl;

/**
 * InheritableThreadLocalSecurityContextHolderStrategy
 * 子线程支持
 *
 * @author Jin
 */
public final class InheritableThreadLocalSecurityContextHolderStrategy implements SecurityContextHolderStrategy {

    private static final ThreadLocal<SecurityContext> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    @Override
    public void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    @Override
    public SecurityContext getContext() {
        SecurityContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    @Override
    public void setContext(SecurityContext context) {
        Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
        CONTEXT_HOLDER.set(context);
    }

    @Override
    public SecurityContext createEmptyContext() {
        return new SecurityContextImpl();
    }

}