package com.lamuda.cloud.websocket.event;

import com.lamuda.cloud.core.principal.LoginUser;
import com.lamuda.cloud.websocket.WsSessionInfo;
import com.lamuda.cloud.websocket.repository.WebSocketChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * DefaultConnectEventServiceImpl
 *
 * @author jpjoo
 */
@Slf4j
public class DefaultConnectEventServiceImpl implements WsConnectEventService {

    private final WebSocketChannelRepository repository;

    public DefaultConnectEventServiceImpl(WebSocketChannelRepository repository) {
        this.repository = repository;
    }

    @Override
    public void connectedEvent(WsSessionInfo<SessionConnectedEvent> info) {
        LoginUser user = (LoginUser) info.getUser();
        String sid = info.getSessionId();
        String framework = info.getFramework();
        if (user != null && framework != null) {
            String uid = user.getUsername();
            repository.add(uid, sid);
            log.debug("Connected -> [sid : {}, uid : {}]", sid, uid);
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
