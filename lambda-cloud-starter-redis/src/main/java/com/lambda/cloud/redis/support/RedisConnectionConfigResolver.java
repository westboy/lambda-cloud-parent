package com.lambda.cloud.redis.support;

import com.lambda.cloud.redis.model.ConnectionInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * RedisConnectionHelper
 *
 * @author westboy
 */
@Slf4j
public class RedisConnectionConfigResolver {
    private final RedisProperties properties;
    private final ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider;
    private final ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    public RedisConnectionConfigResolver(
            RedisProperties properties,
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
        this.properties = properties;
        this.sentinelConfigurationProvider = sentinelConfigurationProvider;
        this.clusterConfigurationProvider = clusterConfigurationProvider;
    }

    public final RedisStandaloneConfiguration getStandaloneConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (StringUtils.hasText(this.properties.getUrl())) {
            ConnectionInfo connectionInfo = parseUrl(this.properties.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            String password = connectionInfo.password();
            checkPassword(password);
            config.setPassword(RedisPassword.of(password));
        } else {
            config.setHostName(this.properties.getHost());
            config.setPort(this.properties.getPort());
            String password = this.properties.getPassword();
            checkPassword(password);
            config.setPassword(RedisPassword.of(password));
        }
        config.setDatabase(this.properties.getDatabase());
        return config;
    }

    public final RedisSentinelConfiguration getSentinelConfig() {
        if (this.properties.getSentinel() == null) {
            return null;
        }
        return this.sentinelConfigurationProvider.getIfAvailable(() -> {
            RedisProperties.Sentinel sentinelProperties = this.properties.getSentinel();
            if (sentinelProperties != null) {
                RedisSentinelConfiguration config = new RedisSentinelConfiguration();
                config.master(sentinelProperties.getMaster());
                config.setSentinels(createSentinels(sentinelProperties));
                String password = this.properties.getPassword();
                checkPassword(password);
                if (StringUtils.hasLength(password)) {
                    config.setPassword(RedisPassword.of(password));
                }
                config.setDatabase(this.properties.getDatabase());
                return config;
            }
            return null;
        });
    }

    public final RedisClusterConfiguration getClusterConfiguration() {
        if (this.properties.getCluster() == null) {
            return null;
        }
        return clusterConfigurationProvider.getIfAvailable(() -> {
            RedisProperties.Cluster clusterProperties = this.properties.getCluster();
            RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
            if (clusterProperties.getMaxRedirects() != null) {
                config.setMaxRedirects(clusterProperties.getMaxRedirects());
            }
            String password = this.properties.getPassword();
            checkPassword(password);
            if (password != null) {
                config.setPassword(RedisPassword.of(password));
            }
            return config;
        });
    }

    private List<RedisNode> createSentinels(RedisProperties.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : sentinel.getNodes()) {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.notNull(parts, "Must be defined as 'host:port'");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis sentinel " + "property '" + node + "'", ex);
            }
        }
        return nodes;
    }

    public ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            boolean useSsl = (url.startsWith("rediss://"));
            String password = null;
            if (uri.getUserInfo() != null) {
                password = uri.getUserInfo();
                int index = password.indexOf(':');
                if (index >= 0) {
                    password = password.substring(index + 1);
                }
            }
            return new ConnectionInfo(uri, useSsl, password);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Malformed url '" + url + "'", ex);
        }
    }

    protected void checkPassword(String password) {
        if (!StringUtils.hasLength(password)) {
            log.warn("redis没有设置密码，有安全风险");
        }
    }
}
