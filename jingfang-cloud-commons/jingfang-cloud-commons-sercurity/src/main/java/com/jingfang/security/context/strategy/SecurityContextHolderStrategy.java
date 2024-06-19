package com.jingfang.security.context.strategy;

import com.jingfang.security.context.SecurityContext;

public interface SecurityContextHolderStrategy {
    void clearContext();

    SecurityContext getContext();

    void setContext(SecurityContext context);

    SecurityContext createEmptyContext();
}