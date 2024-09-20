package com.jingfang.cloud.core.principal;

import java.security.Principal;

/**
 * Principal
 *
 * @author jpjoo
 */
public interface LoginUser extends Principal {

    /**
     * username
     *
     * @return
     */
    String getUsername();

    /**
     * password
     *
     * @return
     */

    String getCredentials();

    /**
     * AccountLocked
     *
     * @return
     */
    Boolean getAccountLocked();

    /**
     * AccountExpired
     *
     * @return
     */
    Boolean getAccountExpired();

}
