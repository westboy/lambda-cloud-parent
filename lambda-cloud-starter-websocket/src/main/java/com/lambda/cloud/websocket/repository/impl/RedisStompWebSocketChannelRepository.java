package com.lambda.cloud.websocket.repository.impl;

import com.google.common.collect.Lists;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.web.TenantHolder;
import com.lambda.cloud.websocket.repository.StompWebSocketChannelRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * RedisWebSocketChannelRepository
 *
 * @author jpjoo
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "RedisWebSocketChannelRepository")
@Slf4j
public record RedisStompWebSocketChannelRepository(StringRedisTemplate template)
        implements StompWebSocketChannelRepository {

    private static final String KEY = "lambda:websocket:online_user:";
    private static final String ONLINE_KEY = "lambda:websocket:online_users";
    private static final String SCRIPT1 =
            "if redis.call('SADD', KEYS[1], ARGV[1]) == 1 then return redis.call('SADD', KEYS[2], ARGV[2]) else return 0 end";
    private static final String SCRIPT2 =
            "if redis.call('DEL', KEYS[1]) == 1 then return redis.call('SREM', KEYS[2]) else return 0 end";
    private static final String SCRIPT3 =
            "if redis.call('SREM', KEYS[1], ARGV[1]) == 1 then return redis.call('SREM', KEYS[2], ARGV[2]) else return 0 end";

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
    public Set<String> getOnlineUsers(Set<String> ids) {
        return template.boundSetOps(getOnlineKey()).union(ids);
    }

    private String getUserKey(String uid) {
        String tenantId = TenantHolder.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            tenantId = Constants.SYSTEM;
        }
        return KEY + tenantId + Constants.COLON + uid;
    }

    private String getOnlineKey() {
        String tenantId = TenantHolder.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            tenantId = Constants.SYSTEM;
        }
        return ONLINE_KEY + Constants.COLON + tenantId;
    }
}
