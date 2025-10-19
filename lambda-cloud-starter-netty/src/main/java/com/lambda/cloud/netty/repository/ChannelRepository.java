package com.lambda.cloud.netty.repository;

import io.netty.channel.Channel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChannelRepository
 */
public class ChannelRepository {
    private final ConcurrentHashMap<String, Channel> channelCache = new ConcurrentHashMap<>();

    public void put(String key, Channel value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }
        channelCache.put(key, value);
    }

    public Optional<Channel> get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return Optional.ofNullable(channelCache.get(key));
    }

    public void remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        channelCache.remove(key);
    }

    public synchronized int size() {
        return channelCache.size();
    }

    public synchronized void clear() {
        channelCache.clear();
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return channelCache.containsKey(key);
    }

    public Map<String, Channel> getChannels() {
        return Collections.unmodifiableMap(channelCache);
    }
}
