package com.lambda.cloud.websocket.repository;

import java.util.Set;

/**
 * WebSocketChannelRepository
 *
 * @author jpjoo
 */
public interface StompWebSocketChannelRepository {

    /**
     * 增加用户
     *
     * @param uid:
     * @param sid:
     */
    void add(String uid, String sid);

    /**
     * 删除用户
     *
     * @param uid:
     */
    void removeAll(String uid);

    /**
     * 删除用户会话
     *
     * @param uid:
     * @param sid:
     */
    void remove(String uid, String sid);

    /**
     * 获取用户
     *
     * @param uid:
     *
     * @return
     */
    Set<String> get(String uid);

    /**
     * 是否存在
     *
     * @param uid
     * @return boolean
     */
    boolean exist(String uid);

    /**
     * 获取在线用户数
     *
     * @return
     */
    long size();

    /**
     * 获取所有的在线用户
     *
     * @param
     * @return java.util.Set<java.lang.String>
     */
    Set<String> getOnlineUsers();

    /**
     * 批量检测用户是否在线
     *
     * @param uids
     * @return java.util.Set<java.lang.String>
     */
    Set<String> getOnlineUsers(Set<String> uids);
}
