package com.jingfang.cloud.websocket.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * DefaultWebSocketChannelRepository
 *
 * @author jpjoo
 */
@Slf4j
public class DefaultWebSocketChannelRepository implements WebSocketChannelRepository {


    private final Cache<String, Set<String>> cache1;


    public DefaultWebSocketChannelRepository(long timeout) {
        this.cache1 = Caffeine.newBuilder()
                .refreshAfterWrite(timeout / 2, TimeUnit.SECONDS)
                .expireAfterWrite(timeout, TimeUnit.SECONDS)
                .build(new CacheLoader<String, Set<String>>() {
                    @Override
                    public @Nullable Set<String> load(@NonNull String uid) {
                        Set<String> sesssions = cache1.getIfPresent(uid);
                        if (sesssions != null) {
                            log.debug("reloading... {}: {}", uid, sesssions);
                        }
                        return sesssions;
                    }
                });
    }

    @Override
    public void add(String uid, String sid) {
        Set<String> sessions = cache1.get(uid, i -> new HashSet<>());
        Objects.requireNonNull(sessions);
        sessions.add(sid);
        cache1.put(uid, sessions);
    }


    @Override
    public void removeAll(String uid) {
        cache1.invalidate(uid);
    }

    @Override
    public void remove(String uid, String sid) {
        Set<String> sessions = cache1.getIfPresent(uid);
        if (Objects.nonNull(sessions)) {
            sessions.remove(sid);
            if (sessions.isEmpty()) {
                cache1.invalidate(uid);
            } else {
                cache1.put(uid, sessions);
            }
        }
    }

    @Override
    public Set<String> get(String uid) {
        return cache1.getIfPresent(uid);
    }

    @Override
    public boolean exist(String uid) {
        Set<String> sessions = get(uid);
        return CollectionUtils.isNotEmpty(sessions);
    }

    @Override
    public long size() {
        return cache1.asMap().keySet().size();
    }

    @Override
    public Set<String> getOnlineUsers() {
        Set<String> onlineUsers = Sets.newHashSet();
        cache1.asMap().forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v)) {
                onlineUsers.add(k);
            }
        });
        return onlineUsers;
    }

    @Override
    public Set<String> getOnlineUsers(Set<String> uids) {
        Set<String> onlineUsers = Sets.newHashSet();
        cache1.getAllPresent(uids).forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v)) {
                onlineUsers.add(k);
            }
        });
        return onlineUsers;
    }
}
