package com.jingfang.security.context;

import com.jingfang.cloud.core.principal.Principal;

import java.io.Serializable;

/**
 * SecurityContext
 *
 * @author jpjoo
 */
public interface SecurityContext extends Serializable {


    /**
     * 获取用户
     *
     * @return
     */
    Principal getPrincipal();

    /**
     * 设置用户
     *
     * @param principal
     */
    void setPrincipal(Principal principal);
}