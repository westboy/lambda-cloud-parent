# lambda-cloud-starter-kafka Kafka扩展模块

## 功能概述
本模块扩展Spring Kafka功能，提供以下增强特性：
1. 延迟消息处理机制
2. 消息监控和管理功能
3. 自定义消息模板

## 核心组件
### 延迟消息处理
- DelayKafkaInitializer: 初始化延迟队列
- DelayKafkaTemplate: 发送延迟消息
- DelayMonitorService: 监控延迟消息
- DelayTimeoutService: 处理超时消息

### 核心模型
- DelayConsumerRecord: 延迟消息记录
- DelayEntry: 延迟条目
- DelayLevel: 延迟级别枚举
- DelayTopicPartition: 主题分区信息

## 配置方式
本模块通过自动配置扩展Spring Kafka，无需额外配置即可启用基础功能。

如需自定义配置，可通过以下属性调整：

```properties
# 启用/禁用延迟功能
spring.kafka.delay.enabled=true

```

## 使用示例
1. 发送延迟消息：
```java
@Autowired
private DelayKafkaTemplate<String, String> delayKafkaTemplate;

public void sendDelayedMessage(String topic, String message) {
    delayKafkaTemplate.send(topic, message, DelayLevel.MINUTE_1);
}
```

2. 处理超时消息：
```java
@Service
public class MyTimeoutHandler implements DelayTimeoutHandler {
    @Override
    public void handleTimeout(DelayConsumerRecord<?,?> record) {
        // 自定义处理逻辑
    }
}
```

## 注意事项
1. 需要正确配置Spring Kafka基础属性
2. 确保Kafka集群可用
3. 延迟级别设置应考虑业务需求
4. 分区数量影响并发处理能力
