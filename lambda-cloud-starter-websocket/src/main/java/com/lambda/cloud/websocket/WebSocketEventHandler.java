package com.lambda.cloud.websocket;

import com.lambda.cloud.websocket.event.WebSocketSubscribeEvent;
import com.lambda.cloud.websocket.service.WebSocketConnectEventService;
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
public class WebSocketEventHandler {

    private final List<WebSocketConnectEventService> connectEventServices;

    private final Map<String, List<WebSocketSubscribeEvent>> subscribeListMap;

    public WebSocketEventHandler(
            List<WebSocketConnectEventService> connectEventServices, List<WebSocketSubscribeEvent> subscribeEvents) {
        this.connectEventServices = connectEventServices;
        this.subscribeListMap = new HashMap<>(6);
        for (WebSocketSubscribeEvent s : subscribeEvents) {
            if (s.topics() == null) {
                continue;
            }
            for (String topic : s.topics()) {
                List<WebSocketSubscribeEvent> events;
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
        WebSocketSession<SessionConnectEvent> info = new WebSocketSession<>(event);
        this.connectEventServices.forEach(i -> i.connectEvent(info));
    }

    /**
     * 建立连接完成
     *
     * @param event:
     */
    @EventListener
    public void connectedEvent(SessionConnectedEvent event) {
        WebSocketSession<SessionConnectedEvent> info = new WebSocketSession<>(event);
        this.connectEventServices.forEach(i -> i.connectedEvent(info));
    }

    /**
     * 关闭连接（包括浏览器关闭当前tab页、刷新等）
     *
     * @param event:
     */
    @EventListener
    public void disconnectEvent(SessionDisconnectEvent event) {
        WebSocketSession<SessionDisconnectEvent> info = new WebSocketSession<>(event);
        this.connectEventServices.forEach(i -> i.disconnectEvent(info));
    }

    /**
     * 订阅事件
     *
     * @param event:
     */
    @EventListener
    public void subscribeEvent(SessionSubscribeEvent event) {
        WebSocketSession<SessionSubscribeEvent> info = new WebSocketSession<>(event);
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
        WebSocketSession<SessionUnsubscribeEvent> info = new WebSocketSession<>(event);
        if (this.subscribeListMap.containsKey(info.getTopic())) {
            this.subscribeListMap.get(info.getTopic()).forEach(i -> i.unsubscribeEvent(info));
        }
    }
}
