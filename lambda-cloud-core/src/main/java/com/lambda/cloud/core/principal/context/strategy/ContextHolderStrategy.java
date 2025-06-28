package com.lambda.cloud.core.principal.context.strategy;

import com.lambda.cloud.core.principal.context.LoginUserContext;

/**
 * SecurityContextHolderStrategy
 *
 * @author jpjoo
 */
public interface ContextHolderStrategy {

    /**
     * 清理
     */
    void clearContext();

    /**
     * 获取上下文
     * @return
     */
    LoginUserContext getContext();

    /**
     * 设置上下文
     * @param context
     */
    void setContext(LoginUserContext context);

    /**
     * 创建一个空的上下文
     * @return
     */
    LoginUserContext createEmptyContext();
}
