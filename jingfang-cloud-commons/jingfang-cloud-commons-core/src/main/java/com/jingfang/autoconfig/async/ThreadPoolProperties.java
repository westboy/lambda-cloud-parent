package com.jingfang.autoconfig.async;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jin
 */
@Configuration
@Data
@ConfigurationProperties("jingfang.cloud.thread-pool")
public class ThreadPoolProperties {

    /**
     * 线程池最大线程数量,除了核心线程外其他线程都会在keepAliveSeconds到期时自动退出
     */
    private int maxPoolSize = 50;

    /**
     * 线程池核心线程数量，即常驻线程，不受keepAliveSeconds影响
     */
    private int corePoolSize = 5;

    /**
     * 队列大小，corePoolSize<当前线程数量<最大线程数量时将会加入任务队列
     */
    private int queueCapacity = 200;

    /**
     * 线程名称前缀
     */
    private String threadNamePrefix = "async-event-";

    /**
     * 空闲线程存活时间
     */
    private int keepAliveSeconds = 30;




}
