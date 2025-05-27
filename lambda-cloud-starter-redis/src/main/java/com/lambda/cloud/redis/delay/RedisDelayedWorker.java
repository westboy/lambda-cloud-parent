package com.lambda.cloud.redis.delay;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;

/**
 * @author westboy
 */
@Slf4j
public class RedisDelayedWorker<T> implements Runnable {

    private final ExecutorService executors;
    private final RedisDelayedListener<T> listener;

    private RBlockingQueue<T> blockingFairQueue;

    public RedisDelayedWorker(RedisDelayConfig config, RedisDelayedListener<T> listener) {
        this.listener = listener;
        this.executors = getWorks(config);
    }

    public void setBlockingQueue(RBlockingQueue<T> blockingFairQueue) {
        this.blockingFairQueue = blockingFairQueue;
    }

    @Override
    @SuppressWarnings("squid:S2189")
    public void run() {
        try {
            T obj = blockingFairQueue.take();
            executors.execute(() -> listener.execute(obj));
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    private ExecutorService getWorks(@Nonnull RedisDelayConfig config) {
        int size = config.getWorks();
        return new ThreadPoolExecutor(
                size,
                size,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(Short.MAX_VALUE),
                new ThreadFactoryBuilder()
                        .setNameFormat("JingFangCloud DelayedQueueWork-%d")
                        .setDaemon(true)
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
