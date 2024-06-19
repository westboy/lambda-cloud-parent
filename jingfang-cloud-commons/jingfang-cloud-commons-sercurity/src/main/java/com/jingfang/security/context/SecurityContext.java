package com.jingfang.security.context;

import com.jingfang.cloud.core.principal.Principal;

import java.io.Serializable;

public interface SecurityContext extends Serializable {
    Principal getPrincipal();

    void setPrincipal(Principal principal);
}