package com.lambda.cloud.websocket.service.impl;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.websocket.WebSocketSession;
import com.lambda.cloud.websocket.repository.WebSocketChannelRepository;
import com.lambda.cloud.websocket.service.WebSocketConnectEventService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * DefaultConnectEventServiceImpl
 *
 * @author jpjoo
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "DefaultConnectEventServiceImpl")
@Slf4j
public record DefaultWebSocketConnectEventServiceImpl(WebSocketChannelRepository repository)
        implements WebSocketConnectEventService {

    @Override
    public void connectedEvent(WebSocketSession<SessionConnectedEvent> info) {
        LoginUser user = (LoginUser) info.getUser();
        String sid = info.getSessionId();
        String framework = info.getFramework();
        if (user != null && framework != null) {
            String uid = user.getName();
            repository.add(uid, sid);
            log.debug("Websocket Connected -> [sid : {}, uid : {}]", sid, uid);
        }
    }

    @Override
    public void disconnectEvent(WebSocketSession<SessionDisconnectEvent> info) {
        LoginUser user = (LoginUser) info.getUser();
        String sid = info.getSessionId();
        String framework = info.getFramework();
        if (user != null && framework != null) {
            String uid = user.getName();
            repository.remove(uid, sid);
            log.debug("Disconnect -> [sid : {}, uid : {}]", sid, uid);
        }
    }
}
