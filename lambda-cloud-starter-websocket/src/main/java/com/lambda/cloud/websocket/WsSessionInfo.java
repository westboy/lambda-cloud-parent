package com.lambda.cloud.websocket;

import static com.lambda.cloud.websocket.Constants.IP_ADDRESS;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.Principal;
import java.util.Map;
import lombok.Getter;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;

/**
 * WsSessionInfo
 *
 * @author jpjoo
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "WsSessionInfo")
public class WsSessionInfo<T extends AbstractSubProtocolEvent> {

    private final Principal principal;
    private final T event;

    @Getter
    private final String topic;

    @Getter
    private final String sessionId;

    @Getter
    private StompHeaderAccessor connectAccessor;

    @Getter
    private Map<String, Object> sessionAttributes;

    @Getter
    private final StompHeaderAccessor messageAccessor;

    public WsSessionInfo(T event) {
        this.event = event;
        principal = event.getUser();
        this.messageAccessor = StompHeaderAccessor.wrap(event.getMessage());
        this.sessionId = messageAccessor.getSessionId();
        this.topic = messageAccessor.getDestination();
        Object simpConnectMessage = messageAccessor.getHeader(Constants.SIMPE_CONNECT_MESSAGE);
        if (simpConnectMessage != null) {
            this.connectAccessor = StompHeaderAccessor.wrap((Message<?>) simpConnectMessage);
            this.sessionAttributes = this.connectAccessor.getSessionAttributes();
        }
        if (this.sessionAttributes == null) {
            this.sessionAttributes = this.messageAccessor.getSessionAttributes();
        }
    }

    public Principal getUser() {
        return principal;
    }

    public String getIp() {
        return (String) this.getSessionAttribute(IP_ADDRESS);
    }

    public AbstractSubProtocolEvent getEvent() {
        return event;
    }

    public Object getSessionAttribute(String name) {
        if (this.sessionAttributes == null) {
            return null;
        }
        return this.sessionAttributes.get(name);
    }

    public String getFramework() {
        return (String) getSessionAttribute(Constants.X_WEBSOCKET_FRAMEWORK);
    }
}
