package com.jingfang.cloud.websocket.repository;

import com.google.common.collect.Lists;
import com.jingfang.cloud.web.TenantHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.jingfang.cloud.websocket.Constants.SYSTEM;
import static com.jingfang.cloud.websocket.Constants.COLON;


/**
 * RedisWebSocketChannelRepository
 *
 * @author jpjoo
 */
@Slf4j
public class RedisWebSocketChannelRepository implements WebSocketChannelRepository {

    private static final String KEY = "jingfang:onlineuser:";
    private static final String ONLINE_KEY = "jingfang:onlineusers";
    private static final String SCRIPT1 = "if redis.call('SADD', KEYS[1], ARGV[1]) == 1 then return redis.call('SADD', KEYS[2], ARGV[2]) else return 0 end";
    private static final String SCRIPT2 = "if redis.call('DEL', KEYS[1]) == 1 then return redis.call('SREM', KEYS[2]) else return 0 end";
    private static final String SCRIPT3 = "if redis.call('SREM', KEYS[1], ARGV[1]) == 1 then return redis.call('SREM', KEYS[2], ARGV[2]) else return 0 end";
    private final StringRedisTemplate template;


    public RedisWebSocketChannelRepository(StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public void add(String uid, String sid) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SCRIPT1, Long.class);
        List<String> keys = Lists.newArrayList(getUserKey(uid), getOnlineKey());
        template.execute(script, keys, sid, uid);
    }


    @Override
    public void removeAll(String uid) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SCRIPT2, Long.class);
        List<String> keys = Lists.newArrayList(getUserKey(uid), getOnlineKey());
        template.execute(script, keys);
    }

    @Override
    public void remove(String uid, String sid) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SCRIPT3, Long.class);
        List<String> keys = Lists.newArrayList(getUserKey(uid), getOnlineKey());
        template.execute(script, keys, sid, uid);
    }

    @Override
    public Set<String> get(String uid) {
        return template.boundSetOps(getUserKey(uid)).members();
    }

    @Override
    public boolean exist(String uid) {
        Set<String> sessions = template.boundSetOps(getUserKey(uid)).members();
        return CollectionUtils.isNotEmpty(sessions);
    }

    @Override
    public long size() {
        return Optional.ofNullable(template.boundSetOps(getOnlineKey()).size()).orElse(0L);
    }

    @Override
    public Set<String> getOnlineUsers() {
        return template.boundSetOps(getOnlineKey()).members();
    }

    @Override
    public Set<String> getOnlineUsers(Set<String> uids) {
        return template.boundSetOps(getOnlineKey()).union(uids);
    }

    private String getUserKey(String uid) {
        String tenantId = TenantHolder.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            tenantId = SYSTEM;
        }
        return KEY + tenantId + COLON + uid;
    }

    private String getOnlineKey() {
        String tenantId = TenantHolder.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            tenantId = SYSTEM;
        }
        return ONLINE_KEY + COLON + tenantId;
    }
}
