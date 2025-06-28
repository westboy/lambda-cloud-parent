package com.lambda.cloud.core.principal.context;

import com.lambda.cloud.core.principal.LoginUser;
import java.io.Serial;
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
public class LoginUserContextImpl implements LoginUserContext {

    @Serial
    private static final long serialVersionUID = 570L;

    private LoginUser principal;

    public LoginUserContextImpl() {}

    public LoginUserContextImpl(LoginUser loginUser) {
        this.principal = loginUser;
    }
}
