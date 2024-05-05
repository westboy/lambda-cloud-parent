package com.jingfang.autoconfig.async;


import com.jingfang.core.event.JingfangApplicationEventMulticaster;
import com.jingfang.core.event.JingfangSyncListenerMethodCache;
import com.jingfang.core.Constants;
import com.jingfang.core.ThreadPoolProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author Jin
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class JingfangAsyncEventConfigurer {

    @Bean
    public JingfangSyncListenerMethodCache jingfangSyncListenerMethodCache() {
        return new JingfangSyncListenerMethodCache();
    }

    @Bean(name = AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    public ApplicationEventMulticaster simpleApplicationEventMulticaster(
            @Qualifier(Constants.ASYN_EVENT_EXECUTOR) ObjectProvider<Executor> executorProvider,
            JingfangSyncListenerMethodCache jingfangSyncListenerMethodCache,
            ThreadPoolProperties properties) {
        Executor executor = executorProvider.getIfAvailable(() -> {
            ThreadPoolTaskExecutor defaultTaskExecutor = new ThreadPoolTaskExecutor();
            //最大线程数
            defaultTaskExecutor.setMaxPoolSize(properties.getMaxPoolSize());
            //核心线程数
            defaultTaskExecutor.setCorePoolSize(properties.getCorePoolSize());
            //任务队列的大小
            defaultTaskExecutor.setQueueCapacity(properties.getQueueCapacity());
            //线程前缀名
            defaultTaskExecutor.setThreadNamePrefix(properties.getThreadNamePrefix());
            //线程存活时间
            defaultTaskExecutor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
            /*
             * 拒绝处理策略
             * CallerRunsPolicy()：交由调用方线程运行，比如 main 线程。
             * AbortPolicy()：直接抛出异常。
             * DiscardPolicy()：直接丢弃。
             * DiscardOldestPolicy()：丢弃队列中最老的任务。
             */
            defaultTaskExecutor.setRejectedExecutionHandler((r, e) -> {
                if (!e.isShutdown()) {
                    //打印丢弃的任务
                    Runnable poll = e.getQueue().poll();
                    if (poll != null) {
                        log.warn("jingfang-thread-pool abort task : {}", poll);
                    }
                    e.execute(r);
                }
            });
            defaultTaskExecutor.initialize();
            return defaultTaskExecutor;
        });
        JingfangApplicationEventMulticaster eventMulticaster = new JingfangApplicationEventMulticaster(jingfangSyncListenerMethodCache);
        eventMulticaster.setTaskExecutor(executor);
        return eventMulticaster;
    }

}
