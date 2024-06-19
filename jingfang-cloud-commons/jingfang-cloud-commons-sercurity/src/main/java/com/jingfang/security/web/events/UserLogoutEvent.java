package com.jingfang.security.web.events;

import com.jingfang.cloud.core.principal.Principal;
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

    public UserLogoutEvent(Principal principal, long cast) {
        this(principal, cast, "用户退出登录");
    }

    public UserLogoutEvent(Principal principal, long cast, String details) {
        super(principal);
        this.cast = cast;
        this.details = details;
    }

}
