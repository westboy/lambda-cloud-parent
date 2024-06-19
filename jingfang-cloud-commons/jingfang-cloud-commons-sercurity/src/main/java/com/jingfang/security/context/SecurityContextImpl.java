package com.jingfang.security.context;

import com.jingfang.cloud.core.principal.Principal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(of = "principal")
@ToString
public class SecurityContextImpl implements SecurityContext {
    private static final long serialVersionUID = 570L;

    private Principal principal;

    public SecurityContextImpl() {
    }

    public SecurityContextImpl(Principal principal) {
        this.principal = principal;
    }
}