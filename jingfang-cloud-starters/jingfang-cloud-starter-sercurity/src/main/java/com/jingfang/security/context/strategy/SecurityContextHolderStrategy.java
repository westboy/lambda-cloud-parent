package com.jingfang.security.context.strategy;

import com.jingfang.security.context.SecurityContext;

/**
 * SecurityContextHolderStrategy
 *
 * @author jpjoo
 */
public interface SecurityContextHolderStrategy {

    /**
     * 清理
     */
    void clearContext();

    /**
     * 获取上下文
     * @return
     */
    SecurityContext getContext();

    /**
     * 设置上下文
     * @param context
     */
    void setContext(SecurityContext context);

    /**
     * 创建一个空的上下文
     * @return
     */
    SecurityContext createEmptyContext();
}