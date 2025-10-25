package com.lambda.cloud.iotdb;

import com.lambda.cloud.iotdb.handler.MessageHandler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.iotdb.session.subscription.consumer.ISubscriptionTablePullConsumer;
import org.apache.iotdb.session.subscription.consumer.table.SubscriptionTablePullConsumerBuilder;
import org.apache.iotdb.session.subscription.payload.SubscriptionMessage;
import org.apache.tsfile.read.common.RowRecord;

/**
 * IoTDB消费者容器，负责管理订阅消费者的生命周期和消息处理
 *
 * <p>主要功能：
 * <ul>
 *   <li>管理IoTDB Table模式的订阅消费者</li>
 *   <li>处理订阅消息并分发给注册的处理器</li>
 *   <li>提供完整的生命周期管理（启动、停止、状态监控）</li>
 *   <li>支持优雅关闭和资源清理</li>
 * </ul>
 *
 * @author Jin
 */
@Slf4j
public class IotDbConsumerContainer {

    @Getter
    private final String consumerId;

    @Getter
    private final String topic;

    @Getter
    private final String consumerGroupId;

    private final String username;
    private final String password;
    private final long pollTimeoutMs;
    private final int shutdownTimeoutSeconds;

    private final List<MessageHandler> listeners = new CopyOnWriteArrayList<>();
    private final AtomicReference<ContainerState> state = new AtomicReference<>(ContainerState.CREATED);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile ISubscriptionTablePullConsumer pullConsumer;
    private volatile ExecutorService executorService;

    /**
     * 容器状态枚举
     */
    public enum ContainerState {
        CREATED, // 已创建
        STARTING, // 启动中
        RUNNING, // 运行中
        STOPPING, // 停止中
        STOPPED, // 已停止
        ERROR // 错误状态
    }

    /**
     * 构造函数
     *
     * @param consumerId             消费者 ID
     * @param topic                  订阅主题
     * @param consumerGroupId        消费者组 ID
     * @param username               用户名
     * @param password               密码
     * @param pollTimeoutMs          轮询超时时间（毫秒）
     * @param shutdownTimeoutSeconds 关闭超时时间（秒）
     */
    public IotDbConsumerContainer(
            String consumerId,
            String topic,
            String consumerGroupId,
            String username,
            String password,
            long pollTimeoutMs,
            int shutdownTimeoutSeconds) {
        this.consumerId = consumerId;
        this.topic = topic;
        this.consumerGroupId = consumerGroupId;
        this.username = username;
        this.password = password;
        this.pollTimeoutMs = pollTimeoutMs;
        this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;

        log.info("创建IoTDB消费者容器 - consumerId: {}, topic: {}, consumerGroupId: {}", consumerId, topic, consumerGroupId);
    }

    /**
     * 添加消息处理器
     *
     * @param handler 消息处理器
     */
    public void addListener(MessageHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("MessageHandler 不能为 null");
        }
        listeners.add(handler);
        log.debug("添加消息处理器，当前处理器数量: {}", listeners.size());
    }

    /**
     * 启动消费者容器
     *
     * @throws IllegalStateException 如果容器已经启动或处于错误状态
     */
    public synchronized void start() {
        if (!state.compareAndSet(ContainerState.CREATED, ContainerState.STARTING)
                && !state.compareAndSet(ContainerState.STOPPED, ContainerState.STARTING)) {
            throw new IllegalStateException("容器当前状态不允许启动: " + state.get());
        }

        log.info("启动IoTDB消费者容器 - consumerId: {}", consumerId);

        try {
            // 创建消费者
            pullConsumer = new SubscriptionTablePullConsumerBuilder()
                    .consumerId(consumerId)
                    .consumerGroupId(consumerGroupId)
                    .username(username)
                    .password(password)
                    .buildTablePullConsumer();

            // 打开连接并订阅主题
            pullConsumer.open();
            pullConsumer.subscribe(topic);

            // 创建线程池并启动消息轮询
            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "iotdb-consumer-" + consumerId);
                thread.setDaemon(true);
                return thread;
            });
            running.set(true);
            state.set(ContainerState.RUNNING);
            executorService.submit(this::pollMessages);
            log.info("IoTDB消费者容器启动成功 - consumerId: {}, topic: {}", consumerId, topic);
        } catch (Exception e) {
            state.set(ContainerState.ERROR);
            running.set(false);
            log.error("启动IoTDB消费者容器失败 - consumerId: {}", consumerId, e);
            cleanup();
            throw new RuntimeException("启动消费者容器失败", e);
        }
    }

    /**
     * 消息轮询处理逻辑
     */
    private void pollMessages() {
        log.info("开始轮询消息 - consumerId: {}, topic: {}", consumerId, topic);
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                List<SubscriptionMessage> messages = pullConsumer.poll(pollTimeoutMs);
                if (messages != null && !messages.isEmpty()) {
                    log.debug("接收到 {} 条消息 - consumerId: {}", messages.size(), consumerId);
                    processMessages(messages);
                }
            } catch (Exception e) {
                log.error("轮询消息时发生错误 - consumerId: {}", consumerId, e);
                state.set(ContainerState.ERROR);
                running.set(false);
                break;
            }
        }
        log.info("消息轮询结束 - consumerId: {}", consumerId);
    }

    /**
     * 处理接收到的消息
     *
     * @param messages 消息列表
     */
    private void processMessages(List<SubscriptionMessage> messages) {
        for (SubscriptionMessage message : messages) {
            try {
                for (var dataSet : message.getSessionDataSetsHandler()) {
                    while (dataSet.hasNext()) {
                        RowRecord record = dataSet.next();
                        for (MessageHandler handler : listeners) {
                            try {
                                handler.handle(record);
                            } catch (Exception e) {
                                log.error(
                                        "消息处理器处理消息时发生错误 - consumerId: {}, handler: {}",
                                        consumerId,
                                        handler.getClass().getSimpleName(),
                                        e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("处理消息时发生错误 - consumerId: {}", consumerId, e);
            }
        }
    }

    /**
     * 停止消费者容器
     */
    public synchronized void stop() {
        if (!state.compareAndSet(ContainerState.RUNNING, ContainerState.STOPPING)
                && !state.compareAndSet(ContainerState.ERROR, ContainerState.STOPPING)) {
            log.warn("容器当前状态不允许停止: {}", state.get());
            return;
        }

        log.info("停止IoTDB消费者容器 - consumerId: {}", consumerId);
        try {
            // 设置停止标志
            running.set(false);
            // 关闭线程池
            if (executorService != null) {
                executorService.shutdown();
                if (!executorService.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                    log.warn("线程池未在指定时间内关闭，强制关闭 - consumerId: {}", consumerId);
                    executorService.shutdownNow();
                }
            }
            // 清理资源
            cleanup();
            state.set(ContainerState.STOPPED);
            log.info("IoTDB消费者容器停止成功 - consumerId: {}", consumerId);
        } catch (Exception e) {
            state.set(ContainerState.ERROR);
            log.error("停止IoTDB消费者容器时发生错误 - consumerId: {}", consumerId, e);
            throw new RuntimeException("停止消费者容器失败", e);
        }
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        if (pullConsumer != null) {
            try {
                pullConsumer.unsubscribe();
                log.debug("取消订阅成功 - consumerId: {}", consumerId);
            } catch (Exception e) {
                log.warn("取消订阅时发生错误 - consumerId: {}", consumerId, e);
            }

            try {
                pullConsumer.close();
                log.debug("关闭消费者连接成功 - consumerId: {}", consumerId);
            } catch (Exception e) {
                log.warn("关闭消费者连接时发生错误 - consumerId: {}", consumerId, e);
            }
            pullConsumer = null;
        }
    }

    /**
     * 获取容器状态
     *
     * @return 容器状态
     */
    public ContainerState getState() {
        return state.get();
    }

    /**
     * 检查容器是否正在运行
     *
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return state.get() == ContainerState.RUNNING && running.get();
    }

    /**
     * 获取注册的处理器数量
     *
     * @return 处理器数量
     */
    public int getListenerCount() {
        return listeners.size();
    }
}
