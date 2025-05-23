package com.lambda.cloud.websocket.event;

import com.lambda.cloud.websocket.WsSessionInfo;
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
public class WsEventHandler {

    private final List<WsConnectEventService> connectEventServices;

    private final Map<String, List<WsSubscribeEvent>> subscribeListMap;

    public WsEventHandler(List<WsConnectEventService> connectEventServices, List<WsSubscribeEvent> subscribeEvents) {
        this.connectEventServices = connectEventServices;
        this.subscribeListMap = new HashMap<>(6);
        for (WsSubscribeEvent s : subscribeEvents) {
            if (s.topics() == null) {
                continue;
            }
            for (String topic : s.topics()) {
                List<WsSubscribeEvent> events;
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
        WsSessionInfo<SessionConnectEvent> info = new WsSessionInfo<>(event);
        this.connectEventServices.forEach(i -> i.connectEvent(info));
    }

    /**
     * 建立连接完成
     *
     * @param event:
     */
    @EventListener
    public void connectedEvent(SessionConnectedEvent event) {
        WsSessionInfo<SessionConnectedEvent> info = new WsSessionInfo<>(event);
        this.connectEventServices.forEach(i -> i.connectedEvent(info));
    }

    /**
     * 关闭连接（包括浏览器关闭当前tab页、刷新等）
     *
     * @param event:
     */
    @EventListener
    public void disconnectEvent(SessionDisconnectEvent event) {
        WsSessionInfo<SessionDisconnectEvent> info = new WsSessionInfo<>(event);
        this.connectEventServices.forEach(i -> i.disconnectEvent(info));
    }

    /**
     * 订阅事件
     *
     * @param event:
     */
    @EventListener
    public void subscribeEvent(SessionSubscribeEvent event) {
        WsSessionInfo<SessionSubscribeEvent> info = new WsSessionInfo<>(event);
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
        WsSessionInfo<SessionUnsubscribeEvent> info = new WsSessionInfo<>(event);
        if (this.subscribeListMap.containsKey(info.getTopic())) {
            this.subscribeListMap.get(info.getTopic()).forEach(i -> i.unsubscribeEvent(info));
        }
    }
}
