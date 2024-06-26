package com.jingfang.security.context;

import com.jingfang.cloud.core.principal.LoginUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SecurityContextImpl
 *
 * @author jpjoo
 */
@Getter
@Setter
@EqualsAndHashCode(of = "principal")
@ToString
public class SecurityContextImpl implements SecurityContext {
    private static final long serialVersionUID = 570L;

    private LoginUser principal;

    public SecurityContextImpl() {
    }

    public SecurityContextImpl(LoginUser loginUser) {
        this.principal = loginUser;
    }
}