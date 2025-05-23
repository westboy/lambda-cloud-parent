package com.lambda.cloud.websocket.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * DefaultWebSocketChannelRepository
 *
 * @author jpjoo
 */
@Slf4j
public class DefaultWebSocketChannelRepository implements WebSocketChannelRepository {

    private final Cache<String, Set<String>> localCache;

    public DefaultWebSocketChannelRepository(long timeout) {
        this.localCache = Caffeine.newBuilder()
                .refreshAfterWrite(timeout / 2, TimeUnit.SECONDS)
                .expireAfterWrite(timeout, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public @Nullable Set<String> load(@NonNull String uid) {
                        Set<String> sessions = localCache.getIfPresent(uid);
                        if (sessions != null) {
                            log.debug("reloading... {}: {}", uid, sessions);
                        }
                        return sessions;
                    }
                });
    }

    @Override
    public void add(String uid, String sid) {
        Set<String> sessions = localCache.get(uid, e -> new HashSet<>());
        if (sessions != null) {
            sessions.add(sid);
            localCache.put(uid, sessions);
        } else {
            log.error("add session failed, uid: {}, sid: {}", uid, sid);
        }
    }

    @Override
    public void removeAll(String uid) {
        localCache.invalidate(uid);
    }

    @Override
    public void remove(String uid, String sid) {
        Set<String> sessions = localCache.getIfPresent(uid);
        if (Objects.nonNull(sessions)) {
            sessions.remove(sid);
            if (sessions.isEmpty()) {
                localCache.invalidate(uid);
            } else {
                localCache.put(uid, sessions);
            }
        }
    }

    @Override
    public Set<String> get(String uid) {
        return localCache.getIfPresent(uid);
    }

    @Override
    public boolean exist(String uid) {
        Set<String> sessions = get(uid);
        return CollectionUtils.isNotEmpty(sessions);
    }

    @Override
    public long size() {
        return localCache.asMap().keySet().size();
    }

    @Override
    public Set<String> getOnlineUsers() {
        Set<String> onlineUsers = Sets.newHashSet();
        localCache.asMap().forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v)) {
                onlineUsers.add(k);
            }
        });
        return onlineUsers;
    }

    @Override
    public Set<String> getOnlineUsers(Set<String> uids) {
        Set<String> onlineUsers = Sets.newHashSet();
        localCache.getAllPresent(uids).forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v)) {
                onlineUsers.add(k);
            }
        });
        return onlineUsers;
    }
}
