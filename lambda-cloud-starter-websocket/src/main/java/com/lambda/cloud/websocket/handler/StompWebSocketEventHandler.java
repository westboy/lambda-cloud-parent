package com.lambda.cloud.websocket.handler;

import com.lambda.cloud.websocket.event.StompWebSocketSubscribeEvent;
import com.lambda.cloud.websocket.service.StompWebSocketConnectEventService;
import com.lambda.cloud.websocket.session.StompWebSocketSession;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.*;

/**
 * WsEventHandler
 *
 * @author jpjoo
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "WsEventHandler")
@Slf4j
public class StompWebSocketEventHandler {

    private final List<StompWebSocketConnectEventService> connectEventServices;

    private final Map<String, List<StompWebSocketSubscribeEvent>> subscribeListMap;

    public StompWebSocketEventHandler(
            List<StompWebSocketConnectEventService> connectEventServices,
            List<StompWebSocketSubscribeEvent> subscribeEvents) {
        this.connectEventServices = connectEventServices;
        this.subscribeListMap = new HashMap<>(6);
        for (StompWebSocketSubscribeEvent s : subscribeEvents) {
            if (s.topics() == null) {
                continue;
            }
            for (String topic : s.topics()) {
                List<StompWebSocketSubscribeEvent> events;
                if (this.subscribeListMap.containsKey(topic)) {
                    events = this.subscribeListMap.get(topic);
                } else {
                    events = new ArrayList<>();
                    this.subscribeListMap.put(topic, events);
                }
                events.add(s);
            }
        }
    }

    /**
     * 正在建立连接
     *
     * @param event:
     */
    @EventListener
    public void connectEvent(SessionConnectEvent event) {
        StompWebSocketSession<SessionConnectEvent> info = new StompWebSocketSession<>(event);
        this.connectEventServices.forEach(i -> i.connectEvent(info));
    }

    /**
     * 建立连接完成
     *
     * @param event:
     */
    @EventListener
    public void connectedEvent(SessionConnectedEvent event) {
        StompWebSocketSession<SessionConnectedEvent> info = new StompWebSocketSession<>(event);
        this.connectEventServices.forEach(i -> i.connectedEvent(info));
    }

    /**
     * 关闭连接（包括浏览器关闭当前tab页、刷新等）
     *
     * @param event:
     */
    @EventListener
    public void disconnectEvent(SessionDisconnectEvent event) {
        StompWebSocketSession<SessionDisconnectEvent> info = new StompWebSocketSession<>(event);
        this.connectEventServices.forEach(i -> i.disconnectEvent(info));
    }

    /**
     * 订阅事件
     *
     * @param event:
     */
    @EventListener
    public void subscribeEvent(SessionSubscribeEvent event) {
        StompWebSocketSession<SessionSubscribeEvent> info = new StompWebSocketSession<>(event);
        if (this.subscribeListMap.containsKey(info.getTopic())) {
            this.subscribeListMap.get(info.getTopic()).forEach(i -> i.subscribeEvent(info));
        }
    }

    /**
     * 取消订阅事件
     *
     * @param event:
     */
    @EventListener
    public void unsubscribeEvent(SessionUnsubscribeEvent event) {
        StompWebSocketSession<SessionUnsubscribeEvent> info = new StompWebSocketSession<>(event);
        if (this.subscribeListMap.containsKey(info.getTopic())) {
            this.subscribeListMap.get(info.getTopic()).forEach(i -> i.unsubscribeEvent(info));
        }
    }
}
