package com.lamuda.security.web.events;

import com.lamuda.cloud.core.principal.LoginUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * UserLoginEvent
 *
 * @author Jin
 */
@Getter
public class UserLoginEvent extends ApplicationEvent {

    private final long cast;
    private final String details;
    private final String remoteAddr;
    private final int remotePort;

    public UserLoginEvent(LoginUser loginUser, long cast, String remoteAddr, int remotePort) {
        this(loginUser, cast, "登录成功", remoteAddr, remotePort);
    }

    public UserLoginEvent(LoginUser loginUser, long cast, String details, String remoteAddr, int remotePort) {
        super(loginUser);
        this.cast = cast;
        this.details = details;
        this.remoteAddr = remoteAddr;
        this.remotePort = remotePort;
    }
}
