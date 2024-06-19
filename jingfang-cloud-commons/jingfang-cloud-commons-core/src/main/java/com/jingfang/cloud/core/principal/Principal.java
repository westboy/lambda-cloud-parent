package com.jingfang.cloud.core.principal;

import java.io.Serializable;
import java.util.Set;

/**
 * Principal
 *
 * @author jpjoo
 */
public interface Principal {

    /**
     * username
     * @return
     */
    Serializable getUsername();

    /**
     * password
     * @return
     */
    String getCredentials();

    /**
     * roles
     *
     * @return
     */
    Set<Serializable> getRoles();

    /**
     * permissions
     *
     * @return
     */
    Set<Serializable> getPermissions();

}
