package com.lambda.cloud.redis.delay;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lambda.cloud.core.exception.NotSupportedException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

/**
 * @author westboy
 */
@Slf4j
@SuppressWarnings("all")
public class RedisDelayedQueueManager<T> implements CommandLineRunner, InitializingBean, DisposableBean {
    private final RedisDelayConfig config;
    private final RedisDelayedListener<T> listener;

    private ObjectProvider<RedissonClient> redissonProvider;

    private RDelayedQueue<T> delayedQueue;
    private RBlockingQueue<T> blockingFairQueue;
    private ScheduledExecutorService scheduler;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    public RedisDelayedQueueManager(RedisDelayConfig config, RedisDelayedListener<T> listener) {
        this.config = config;
        this.listener = listener;
    }

    @Autowired
    public void setRedissonProvider(ObjectProvider<RedissonClient> redissonProvider) {
        this.redissonProvider = redissonProvider;
    }

    public void offer(T object) {
        delayedQueue.offer(object, config.getDelay(), config.getTimeUnit());
    }

    public void offer(T object, int delay) {
        delayedQueue.offer(object, delay, TimeUnit.MILLISECONDS);
    }

    public void offer(T object, int delay, TimeUnit unit) {
        delayedQueue.offer(object, delay, unit);
    }

    @Override
    public void run(String... args) {
        scheduler = new ScheduledThreadPoolExecutor(
                1,
                new ThreadFactoryBuilder()
                        .setNameFormat("lambda cloud delayed queue Job-%d")
                        .setDaemon(true)
                        .build());
        RedisDelayedWorker<T> worker = new RedisDelayedWorker<>(config, listener);
        worker.setBlockingQueue(blockingFairQueue);
        scheduler.scheduleWithFixedDelay(worker, 0L, 100, MILLISECONDS);
    }

    @Override
    public void destroy() {
        Objects.requireNonNull(redissonProvider.getIfAvailable()).isShutdown();
        scheduler.shutdown();
    }

    @Override
    public void afterPropertiesSet() {
        if (redissonProvider.getIfAvailable() != null) {
            RedissonClient redissonClient = Objects.requireNonNull(redissonProvider.getIfAvailable());
            this.blockingFairQueue = redissonClient.getBlockingQueue(config.getName());
            this.delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
        } else {
            throw new NotSupportedException("如果你想使用延迟队列，请开启此配置: \n spring.data.redis.redisson.enabled: true");
        }
    }
}
