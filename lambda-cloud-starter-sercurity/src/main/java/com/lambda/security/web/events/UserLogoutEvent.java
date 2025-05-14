package com.lambda.security.web.events;

import com.lambda.cloud.core.principal.LoginUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * UserLogoutEvent
 *
 * @author Jin
 */
@Getter
public class UserLogoutEvent extends ApplicationEvent {

    private final long cast;
    private final String details;
    @Setter
    private String ipaddress;

    public UserLogoutEvent(LoginUser loginUser, long cast) {
        this(loginUser, cast, "用户退出登录");
    }

    public UserLogoutEvent(LoginUser loginUser, long cast, String details) {
        super(loginUser);
        this.cast = cast;
        this.details = details;
    }

}
