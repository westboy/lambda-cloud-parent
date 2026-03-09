package com.lambda.autoconfig;

import com.lambda.cloud.core.jackson.LambdaObjectMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.ClientResourcesBuilderCustomizer;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.support.collections.RedisProperties;

/**
 * @author Jin
 */
@Slf4j
@Configuration
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
        RedisSerializer<?> serializer = new GenericJacksonJsonRedisSerializer(objectMapper);
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
        RedisSerializer<?> serializer = new GenericJacksonJsonRedisSerializer(objectMapper);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(STRING_REDIS_SERIALIZER);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);
        redisTemplate.setEnableTransactionSupport(false);
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean({RedisConnectionFactory.class})
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }
}
