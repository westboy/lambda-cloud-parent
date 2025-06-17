package com.lambda.cloud.core.principal;

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
     * @return String
     */
    String getUsername();

    /**
     * password
     *
     * @return String
     */
    String getCredentials();

    /**
     * orgId
     *
     * @return String
     */
    String getOrgId();

    /**
     * AccountLocked
     *
     * @return Boolean
     */
    Boolean getAccountLocked();

    /**
     * AccountExpired
     *
     * @return Boolean
     */
    Boolean getAccountExpired();

    /**
     * tenantId
     *
     * @return String
     */
    String getTenantId();
}
