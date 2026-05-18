---
name: "lambda-cloud-messaging"
description: "Lambda Cloud 消息队列配置指南。当用户需要配置RocketMQ发送/消费消息、类型安全消费或消息转换时调用。"
---

# Lambda Cloud 消息队列

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-rocketmq</artifactId>
</dependency>
```

## 基础配置

```yaml
rocketmq:
  producer:
    endpoints: localhost:8081
    producer-group: default-producer-group
  push-consumer:
    endpoints: localhost:8081
    consumer-group: default-consumer-group
```

## 发送消息

```java
@Service
public class OrderMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    // 同步发送
    public SendResult sendOrderMessage(OrderMessage message) {
        return rocketMQTemplate.syncSend("order-topic", message);
    }

    // 异步发送
    public void sendOrderMessageAsync(OrderMessage message) {
        rocketMQTemplate.asyncSend("order-topic", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("消息发送成功: {}", sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("消息发送失败", e);
            }
        });
    }

    // 单向发送 (不关心结果)
    public void sendOrderMessageOneway(OrderMessage message) {
        rocketMQTemplate.sendOneWay("order-topic", message);
    }

    // 延迟消息
    public SendResult sendDelayMessage(OrderMessage message, int delayLevel) {
        return rocketMQTemplate.syncSend("order-topic", message, 3000, delayLevel);
    }
}
```

## 消费消息

### LambdaRocketMQListener 类型安全消费

```java
@Component
@RocketMQMessageListener(
    topic = "order-topic",
    consumerGroup = "order-consumer-group"
)
public class OrderMessageListener extends LambdaRocketMQListener<OrderMessage> {

    @Override
    public ConsumeResult consume(LambdaMessageView<OrderMessage> messageView) {
        OrderMessage order = messageView.getBody();
        log.info("收到订单消息: {}", order.getOrderId());

        // 处理订单
        processOrder(order);

        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consumeError(MessageView messageView, Exception exception) {
        log.error("消费消息失败", exception);
        return ConsumeResult.FAILURE;
    }
}
```

### LambdaMessageView 消息视图

```java
@Override
public ConsumeResult consume(LambdaMessageView<OrderMessage> messageView) {
    // 获取消息体 (已反序列化)
    OrderMessage body = messageView.getBody();

    // 获取消息元数据
    String messageId = messageView.getMessageId();
    String topic = messageView.getTopic();
    String tag = messageView.getTag();
    Map<String, String> properties = messageView.getProperties();
    Collection<String> keys = messageView.getKeys();

    return ConsumeResult.SUCCESS;
}
```

## 消息模型

```java
@Data
public class OrderMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNo;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createTime;
}
```

**注意**: 消息泛型 `T` 需满足 `Serializable` 约束。

## LambdaRocketMQMessageConverter

统一消息体转换规则:
- 字符串/基本类型: 直接转换
- JSON 对象: 使用 Jackson 反序列化

自动装配, 无需手动配置。

## 生产者分组

```java
@Service
@RocketMQMessageProducer(producerGroup = "order-producer-group")
public class OrderProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void send(OrderMessage message) {
        rocketMQTemplate.syncSend("order-topic:tag1", message);
    }
}
```

## Tag 过滤

```java
// 生产者: 带 Tag 发送
rocketMQTemplate.syncSend("order-topic:created", message);

// 消费者: 过滤 Tag
@RocketMQMessageListener(
    topic = "order-topic",
    consumerGroup = "order-consumer-group",
    selectorExpression = "created"  // 只消费 created tag
)
public class OrderCreatedListener extends LambdaRocketMQListener<OrderMessage> {
    // ...
}
```

## 完整示例

```java
// 消息模型
@Data
public class PayMessage implements Serializable {
    private Long orderId;
    private BigDecimal amount;
    private String payChannel;
}

// 生产者
@Service
public class PayProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendPayMessage(PayMessage message) {
        SendResult result = rocketMQTemplate.syncSend("pay-topic", message);
        log.info("支付消息发送成功: {}", result.getMsgId());
    }
}

// 消费者
@Component
@RocketMQMessageListener(
    topic = "pay-topic",
    consumerGroup = "pay-consumer-group"
)
public class PayMessageListener extends LambdaRocketMQListener<PayMessage> {

    @Override
    public ConsumeResult consume(LambdaMessageView<PayMessage> messageView) {
        PayMessage pay = messageView.getBody();
        log.info("收到支付消息: orderId={}, amount={}", pay.getOrderId(), pay.getAmount());

        // 处理支付结果
        processPayment(pay);

        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consumeError(MessageView messageView, Exception exception) {
        log.error("消费支付消息失败", exception);
        return ConsumeResult.FAILURE;
    }
}
```

## 最佳实践

1. **消息序列化**: 使用 Jackson JSON 序列化, 确保对象有无参构造和标准 getter/setter
2. **消费者幂等**: 消费端应保证幂等, 因为消息可能重复投递
3. **失败重试**: 消费失败返回 `ConsumeResult.FAILURE`, RocketMQ 会自动重试
4. **Tag 使用**: 使用 Tag 过滤消息, 减少不必要的消费
5. **消息大小**: 单条消息不要超过 4MB
6. **延迟消息**: 使用 `delayLevel` 设置延迟级别 (1-18, 对应 1s-2h)
7. **顺序消息**: 需要顺序消费时使用 `MessageQueueSelector`
