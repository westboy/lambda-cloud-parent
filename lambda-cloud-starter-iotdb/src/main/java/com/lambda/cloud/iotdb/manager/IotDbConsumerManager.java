package com.lambda.cloud.iotdb.manager;

import com.lambda.autoconfig.IotDbProperties;
import com.lambda.cloud.iotdb.IotDbConsumerContainer;
import com.lambda.cloud.iotdb.annotation.IotDbSubscription;
import com.lambda.cloud.iotdb.handler.MessageHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * IoTDB消费者管理器，负责管理所有消费者容器的注册、启动和停止
 *
 * <p>主要功能：
 * <ul>
 *   <li>注册带有@IotDbSubscription注解的消息处理器</li>
 *   <li>创建和管理IotDbConsumerContainer实例</li>
 *   <li>提供统一的生命周期管理</li>
 *   <li>支持优雅关闭所有消费者</li>
 * </ul>
 *
 * @author Jin
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class IotDbConsumerManager implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(IotDbConsumerManager.class);

    private ApplicationContext context;
    private final IotDbProperties iotDbProperties;
    private final Map<String, IotDbConsumerContainer> containers = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param iotDbProperties IoTDB配置属性
     */
    public IotDbConsumerManager(IotDbProperties iotDbProperties) {
        this.iotDbProperties = iotDbProperties;
        logger.info("创建IoTDB消费者管理器");
    }

    /**
     * 注册消费者
     *
     * @param listenerClass 监听器类
     * @param annotation 订阅注解
     * @throws Exception 注册失败时抛出异常
     */
    public void register(Class<?> listenerClass, IotDbSubscription annotation) throws Exception {
        String consumerId = annotation.consumerId();

        if (containers.containsKey(consumerId)) {
            logger.warn("消费者ID已存在，跳过注册: {}", consumerId);
            return;
        }

        logger.info(
                "注册IoTDB消费者 - consumerId: {}, topic: {}, consumerGroupId: {}",
                consumerId,
                annotation.topic(),
                annotation.consumerGroupId());

        try {
            // 创建消费者容器
            IotDbConsumerContainer container = new IotDbConsumerContainer(
                    annotation.consumerId(),
                    annotation.topic(),
                    annotation.consumerGroupId(),
                    iotDbProperties.getUser() != null ? iotDbProperties.getUser() : "root",
                    iotDbProperties.getPassword() != null ? iotDbProperties.getPassword() : "root",
                    iotDbProperties.getTimeout(),
                    30 // 默认30秒关闭超时
                    );

            // 添加消息处理器
            if (MessageHandler.class.isAssignableFrom(listenerClass)) {
                MessageHandler handler = (MessageHandler) context.getBean(listenerClass);
                container.addListener(handler);
                logger.debug("添加消息处理器: {} -> {}", consumerId, listenerClass.getSimpleName());
            } else {
                logger.warn("类 {} 未实现 MessageHandler 接口，跳过注册", listenerClass.getName());
                return;
            }

            // 启动容器
            container.start();
            containers.put(consumerId, container);

            logger.info("IoTDB消费者注册并启动成功 - consumerId: {}", consumerId);

        } catch (Exception e) {
            logger.error("注册IoTDB消费者失败 - consumerId: {}", consumerId, e);
            throw new RuntimeException("注册消费者失败: " + consumerId, e);
        }
    }

    /**
     * 获取指定消费者容器
     *
     * @param consumerId 消费者ID
     * @return 消费者容器，如果不存在则返回null
     */
    public IotDbConsumerContainer getContainer(String consumerId) {
        return containers.get(consumerId);
    }

    /**
     * 停止指定消费者
     *
     * @param consumerId 消费者ID
     * @return 是否成功停止
     */
    public boolean stopContainer(String consumerId) {
        IotDbConsumerContainer container = containers.get(consumerId);
        if (container != null) {
            try {
                container.stop();
                containers.remove(consumerId);
                logger.info("停止IoTDB消费者成功 - consumerId: {}", consumerId);
                return true;
            } catch (Exception e) {
                logger.error("停止IoTDB消费者失败 - consumerId: {}", consumerId, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 停止所有消费者容器
     */
    public void stopAll() {
        logger.info("开始停止所有IoTDB消费者容器，总数: {}", containers.size());

        containers.forEach((consumerId, container) -> {
            try {
                container.stop();
                logger.debug("停止消费者成功: {}", consumerId);
            } catch (Exception e) {
                logger.error("停止消费者失败: {}", consumerId, e);
            }
        });

        containers.clear();
        logger.info("所有IoTDB消费者容器已停止");
    }

    /**
     * 获取运行中的消费者数量
     *
     * @return 运行中的消费者数量
     */
    public int getRunningContainerCount() {
        return (int) containers.values().stream()
                .filter(IotDbConsumerContainer::isRunning)
                .count();
    }

    /**
     * 获取总消费者数量
     *
     * @return 总消费者数量
     */
    public int getTotalContainerCount() {
        return containers.size();
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        logger.debug("设置ApplicationContext完成");
    }
}
