package com.lambda.cloud.websocket.event;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.websocket.WsSessionInfo;
import com.lambda.cloud.websocket.repository.WebSocketChannelRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * DefaultConnectEventServiceImpl
 *
 * @author jpjoo
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "DefaultConnectEventServiceImpl")
@Slf4j
public record DefaultConnectEventServiceImpl(WebSocketChannelRepository repository) implements WsConnectEventService {

    @Override
    public void connectedEvent(WsSessionInfo<SessionConnectedEvent> info) {
        LoginUser user = (LoginUser) info.getUser();
        String sid = info.getSessionId();
        String framework = info.getFramework();
        if (user != null && framework != null) {
            String uid = user.getUsername();
            repository.add(uid, sid);
            log.debug("Websocket Connected -> [sid : {}, uid : {}]", sid, uid);
        }
    }

    @Override
    public void disconnectEvent(WsSessionInfo<SessionDisconnectEvent> info) {
        LoginUser user = (LoginUser) info.getUser();
        String sid = info.getSessionId();
        String framework = info.getFramework();
        if (user != null && framework != null) {
            String uid = user.getUsername();
            repository.remove(uid, sid);
            log.debug("Disconnect -> [sid : {}, uid : {}]", sid, uid);
        }
    }
}
