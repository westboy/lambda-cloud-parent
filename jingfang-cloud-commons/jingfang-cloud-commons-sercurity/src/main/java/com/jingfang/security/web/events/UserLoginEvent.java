package com.jingfang.security.web.events;

import com.jingfang.cloud.core.principal.Principal;
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

    public UserLoginEvent(Principal principal, long cast, String remoteAddr, int remotePort) {
        this(principal, cast, "登录成功", remoteAddr, remotePort);
    }

    public UserLoginEvent(Principal principal, long cast, String details, String remoteAddr, int remotePort) {
        super(principal);
        this.cast = cast;
        this.details = details;
        this.remoteAddr = remoteAddr;
        this.remotePort = remotePort;
    }
}
