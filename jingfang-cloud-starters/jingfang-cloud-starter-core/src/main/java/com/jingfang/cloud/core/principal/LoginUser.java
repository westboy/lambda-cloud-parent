package com.jingfang.cloud.core.principal;

import java.security.Principal;
import java.util.Set;

/**
 * Principal
 *
 * @author jpjoo
 */
public interface LoginUser extends Principal {

    /**
     * name
     *
     * @return
     */
    @Override
    default String getName() {
        return getUsername();
    }

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
     * roles
     *
     * @return
     */
    Set<String> getRoles();

    /**
     * permissions
     *
     * @return
     */
    Set<String> getPermissions();

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
