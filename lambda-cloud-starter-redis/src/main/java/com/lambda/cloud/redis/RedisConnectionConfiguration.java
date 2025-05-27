package com.lambda.cloud.redis;

import com.lambda.autoconfig.RedisExtendProperties;
import com.lambda.cloud.redis.model.ConnectionInfo;
import com.lambda.cloud.redis.support.RedisConnectionConfigResolver;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author westboy
 */
@Configuration
@ConditionalOnClass(RedisClient.class)
@ConditionalOnProperty(name = RedisConnectionConfiguration.MODE)
public class RedisConnectionConfiguration {
    public static final String MODE = "spring.redis.mode";

    private final RedisProperties properties;
    private final RedisConnectionConfigResolver redisConnectionConfigResolver;
    private final RedisExtendProperties redisExtendProperties;
    private final ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    public RedisConnectionConfiguration(
            RedisProperties properties,
            RedisExtendProperties redisExtendProperties,
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizersProvider) {
        this.properties = properties;
        this.redisExtendProperties = redisExtendProperties;
        this.builderCustomizers = builderCustomizersProvider;
        this.redisConnectionConfigResolver = new RedisConnectionConfigResolver(
                properties, sentinelConfigurationProvider, clusterConfigurationProvider);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(ClientResources.class)
    public DefaultClientResources lettuceClientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
                clientResources, this.properties.getLettuce().getPool());
        return createLettuceConnectionFactory(clientConfig);
    }

    private LettuceConnectionFactory createLettuceConnectionFactory(LettuceClientConfiguration clientConfiguration) {
        LettuceConnectionFactory factory;
        RedisExtendProperties.Mode mode = redisExtendProperties.getMode();
        switch (mode) {
            case SENTINEL:
                RedisSentinelConfiguration sentinelConfig = redisConnectionConfigResolver.getSentinelConfig();
                Assert.notNull(sentinelConfig, "RedisSentinelConfiguration must not be null");
                factory = new LettuceConnectionFactory(sentinelConfig, clientConfiguration);
                break;
            case CLUSTER:
                RedisClusterConfiguration config = redisConnectionConfigResolver.getClusterConfiguration();
                Assert.notNull(config, "RedisClusterConfiguration must not be null");
                factory = new LettuceConnectionFactory(config, clientConfiguration);
                break;
            default:
                factory = new LettuceConnectionFactory(
                        redisConnectionConfigResolver.getStandaloneConfig(), clientConfiguration);
        }
        return factory;
    }

    private LettuceClientConfiguration getLettuceClientConfiguration(ClientResources clientResources, Pool pool) {
        LettuceClientConfigurationBuilder builder = createBuilder(pool);
        applyProperties(builder);
        if (StringUtils.hasText(this.properties.getUrl())) {
            customizeConfigurationFromUrl(builder);
        }
        builder.clientResources(clientResources);
        customize(builder);
        return builder.build();
    }

    private LettuceClientConfigurationBuilder createBuilder(Pool pool) {
        if (pool == null) {
            return LettuceClientConfiguration.builder();
        }
        return new PoolBuilderFactory().createBuilder(pool);
    }

    private void applyProperties(LettuceClientConfigurationBuilder builder) {
        if (this.properties.getSsl().isEnabled()) {
            builder.useSsl();
        }
        if (this.properties.getTimeout() != null) {
            builder.commandTimeout(this.properties.getTimeout());
        }
        if (this.properties.getLettuce() != null) {
            RedisProperties.Lettuce lettuce = this.properties.getLettuce();
            if (lettuce.getShutdownTimeout() != null
                    && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(this.properties.getLettuce().getShutdownTimeout());
            }
        }
    }

    private void customizeConfigurationFromUrl(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        ConnectionInfo connectionInfo = redisConnectionConfigResolver.parseUrl(this.properties.getUrl());
        if (connectionInfo.useSsl()) {
            builder.useSsl();
        }
    }

    private void customize(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        this.builderCustomizers.orderedStream().forEach(customizer -> customizer.customize(builder));
    }

    /**
     * Inner class to allow optional commons-pool2 dependency.
     */
    private static class PoolBuilderFactory {

        LettuceClientConfigurationBuilder createBuilder(Pool properties) {
            GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = getPoolConfig(properties);
            return LettucePoolingClientConfiguration.builder().poolConfig(poolConfig);
        }

        private GenericObjectPoolConfig<StatefulConnection<?, ?>> getPoolConfig(Pool properties) {
            GenericObjectPoolConfig<StatefulConnection<?, ?>> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(properties.getMaxActive());
            config.setMaxIdle(properties.getMaxIdle());
            config.setMinIdle(properties.getMinIdle());
            if (properties.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRuns(properties.getTimeBetweenEvictionRuns());
            }
            if (properties.getMaxWait() != null) {
                config.setMaxWait(properties.getMaxWait());
            }
            return config;
        }
    }
}
