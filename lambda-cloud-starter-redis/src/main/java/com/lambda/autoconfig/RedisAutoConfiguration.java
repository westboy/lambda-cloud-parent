package com.lambda.autoconfig;

import com.lambda.cloud.core.jackson.mapper.LambdaObjectMapper;
import com.lambda.cloud.redis.RedisConnectionConfiguration;
import com.lambda.cloud.redis.customize.RedissonConfigurationCustomizer;
import com.lambda.cloud.redis.helper.RedisHelper;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SocketOptions.KeepAliveOptions;
import io.lettuce.core.resource.NettyCustomizer;
import io.lettuce.core.resource.Transports;
import io.lettuce.core.resource.Transports.NativeTransports;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Jin
 */
@Slf4j
@Configuration
@Import({RedisConnectionConfiguration.class})
@EnableConfigurationProperties({RedissonProperties.class, RedisProperties.class, RedisExtendProperties.class})
public class RedisAutoConfiguration {

    private static final StringRedisSerializer STRING_REDIS_SERIALIZER = new StringRedisSerializer();
    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";
    /**
     * 使用JDK序列化的模板
     */
    public static final String JDK_REDIS_TEMPLATE = "jdkRedisTemplate";
    /**
     * 使用字符串序列化的模板
     */
    public static final String STRING_REDIS_TEMPLATE = "stringRedisTemplate";
    /**
     * 使用JSON序列化的模板
     */
    public static final String POJO_REDIS_TEMPLATE = "redisTemplate";

    public RedisAutoConfiguration() {
        log.trace("initializing...");
    }

    @Bean
    @ConditionalOnMissingBean
    public LambdaObjectMapper objectMapper() {
        return new LambdaObjectMapper();
    }

    @Bean
    public RedisHelper redisHelper(RedisTemplate<String, Object> redisTemplate) {
        return new RedisHelper(redisTemplate);
    }

    @Bean
    @Description("配置开启keepAlive")
    public LettuceClientConfigurationBuilderCustomizer clientConfigurationBuilderCustomizer() {
        return clientConfigurationBuilder -> {
            // Enabled keep alive
            log.info("Enable keepAlive, channel : {}", Transports.socketChannelClass());
            clientConfigurationBuilder.readFrom(ReadFrom.MASTER);
            KeepAliveOptions keepAliveOptions = NativeTransports.isDomainSocketSupported()
                    ? KeepAliveOptions.builder()
                            .enable(true)
                            .idle(Duration.ofSeconds(15))
                            .count(3)
                            .interval(Duration.ofSeconds(5))
                            .build()
                    : KeepAliveOptions.builder().build();
            SocketOptions socketOptions =
                    SocketOptions.builder().keepAlive(keepAliveOptions).build();
            ClientOptions clientOptions =
                    ClientOptions.builder().socketOptions(socketOptions).build();
            clientConfigurationBuilder.clientOptions(clientOptions);
        };
    }

    /**
     * @see <a href="https://www.cnblogs.com/hushaojun/p/16285486.html">cnblogs</a>
     * @see <a href="https://github.com/lettuce-io/lettuce-core/issues/1428">github</a>
     */
    @Bean
    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    @Description("解决redis使用lettuce间接性超时问题（15分钟左右）")
    public ClientResourcesBuilderCustomizer clientResources() {
        return clientResourcesBuilder -> clientResourcesBuilder.nettyCustomizer(new NettyCustomizer() {

            @Override
            public void afterChannelInitialized(Channel channel) {
                // 30s没有进行读写
                channel.pipeline().addLast(new IdleStateHandler(0, 0, 30));
                channel.pipeline().addLast(new ChannelDuplexHandler() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                        if (evt instanceof IdleStateEvent stateEvent) {
                            if (stateEvent.state() == IdleState.ALL_IDLE) {
                                log.debug("Heartbeat detection triggers disconnection...");
                                ctx.disconnect();
                            }
                        }
                    }
                });
            }
        });
    }

    @Bean(JDK_REDIS_TEMPLATE)
    public RedisTemplate<String, Object> jdkRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(STRING_REDIS_SERIALIZER);
        template.setHashKeySerializer(STRING_REDIS_SERIALIZER);
        return template;
    }

    @Bean(STRING_REDIS_TEMPLATE)
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory, LambdaObjectMapper objectMapper) {
        RedisSerializer<?> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(STRING_REDIS_SERIALIZER);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setEnableTransactionSupport(false);
        return template;
    }

    @Bean(POJO_REDIS_TEMPLATE)
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory, LambdaObjectMapper objectMapper) {
        RedisSerializer<?> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(STRING_REDIS_SERIALIZER);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);
        redisTemplate.setEnableTransactionSupport(false);
        return redisTemplate;
    }

    @Configuration
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnProperty(prefix = "spring.data.redis.redisson", name = "enabled", matchIfMissing = true)
    static class RedissonAutoConfiguration {

        @Lazy
        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean({RedissonClient.class})
        public RedissonClient redisson(
                RedisProperties properties,
                RedissonProperties redissonProperties,
                RedisExtendProperties redisExtendProperties,
                List<RedissonConfigurationCustomizer> redissonConfigurationCustomizers,
                LambdaObjectMapper objectMapper) {
            Config config = new Config();
            config.setCodec(new JsonJacksonCodec(objectMapper));
            int timeout = getTimeout(properties.getTimeout());
            RedisExtendProperties.Mode mode = redisExtendProperties.getMode();
            switch (mode) {
                case SENTINEL:
                    RedisProperties.Sentinel sentinel = properties.getSentinel();
                    List<String> nodes = sentinel.getNodes();
                    SentinelServersConfig sentinelServersConfig = config.useSentinelServers()
                            .setMasterName(sentinel.getMaster())
                            .setDatabase(properties.getDatabase())
                            .setConnectTimeout(timeout)
                            .setKeepAlive(true)
                            .setPingConnectionInterval(redissonProperties.getPingConnectionInterval())
                            .setMasterConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize())
                            .setMasterConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                            .setSlaveConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize())
                            .setSlaveConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                            .addSentinelAddress(convert(nodes));
                    if (StringUtils.isNotBlank(sentinel.getPassword())) {
                        sentinelServersConfig.setPassword(sentinel.getPassword());
                    }
                    break;
                case CLUSTER:
                    RedisProperties.Cluster cluster = properties.getCluster();
                    ClusterServersConfig clusterServersConfig = config.useClusterServers()
                            .setConnectTimeout(timeout)
                            .setKeepAlive(true)
                            .setPingConnectionInterval(redissonProperties.getPingConnectionInterval())
                            .setMasterConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize())
                            .setMasterConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                            .setSlaveConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize())
                            .setSlaveConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                            .addNodeAddress(convert(cluster.getNodes()));

                    if (StringUtils.isNotBlank(properties.getPassword())) {
                        clusterServersConfig.setPassword(properties.getPassword());
                    }
                    break;
                default:
                    String prefix = properties.getSsl().isEnabled() ? REDISS_PROTOCOL_PREFIX : REDIS_PROTOCOL_PREFIX;
                    SingleServerConfig singleServerConfig = config.useSingleServer()
                            .setAddress(prefix + properties.getHost() + ":" + properties.getPort())
                            .setConnectTimeout(timeout)
                            .setKeepAlive(true)
                            .setPingConnectionInterval(redissonProperties.getPingConnectionInterval())
                            .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize())
                            .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                            .setDatabase(redissonProperties.getDatabase());
                    if (StringUtils.isNotBlank(properties.getPassword())) {
                        singleServerConfig.setPassword(properties.getPassword());
                    }
            }
            if (redissonConfigurationCustomizers != null) {
                for (RedissonConfigurationCustomizer customizer : redissonConfigurationCustomizers) {
                    customizer.customize(config);
                }
            }
            return Redisson.create(config);
        }

        @Bean
        @ConditionalOnMissingBean({RedisConnectionFactory.class})
        public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
            return new RedissonConnectionFactory(redisson);
        }

        private static int getTimeout(Duration timeout) {
            if (timeout != null && timeout.toMillis() < Integer.MAX_VALUE) {
                return (int) timeout.toMillis();
            }
            return 10000;
        }

        private static String[] convert(List<String> nodesObject) {
            List<String> nodes = new ArrayList<>(nodesObject.size());
            for (String node : nodesObject) {
                if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
                    nodes.add(REDIS_PROTOCOL_PREFIX + node);
                } else {
                    nodes.add(node);
                }
            }
            return nodes.toArray(new String[0]);
        }
    }
}
