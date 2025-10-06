# Lambda Cloud Starter RocketMQ

RocketMQ 消息队列的 Spring Boot Starter，提供简化的消息监听和类型安全的消息处理。

## 功能特性

- 简化的消息监听器抽象
- 类型安全的消息处理
- 自动消息转换（JSON、字符串、基本类型）
- 增强的消息视图对象
- 自动配置消息转换器

## 依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-rocketmq</artifactId>
</dependency>
```

## 核心组件

### LambdaRocketMQListener
抽象的消息监听器基类，提供类型安全的消息处理：

```java
public abstract class LambdaRocketMQListener<T extends Serializable> {
    public abstract ConsumeResult consume(LambdaMessageView<T> messageView);
    public abstract ConsumeResult consumeError(MessageView messageView, Exception exception);
}
```

### LambdaMessageView
增强的消息视图对象，包含完整的消息信息：

```java
public class LambdaMessageView<T> {
    private T body;                    // 消息体（已转换为目标类型）
    private MessageId messageId;       // 消息ID
    private String topic;              // 主题
    private String tag;                // 标签
    private Collection<String> keys;   // 消息键
    private Map<String, String> properties; // 消息属性
    // ... 其他属性
}
```

### LambdaRocketMQMessageConverter
自定义消息转换器，支持多种数据格式转换。

## 使用示例

### 字符串消息监听器

```java
@Component
@RocketMQMessageListener(
    topic = "test-topic",
    consumerGroup = "test-group"
)
public class StringMessageListener extends LambdaRocketMQListener<String> {
    
    @Override
    public ConsumeResult consume(LambdaMessageView<String> messageView) {
        String message = messageView.getBody();
        System.out.println("Received: " + message);
        return ConsumeResult.SUCCESS;
    }
    
    @Override
    public ConsumeResult consumeError(MessageView messageView, Exception exception) {
        System.err.println("Error processing message: " + exception.getMessage());
        return ConsumeResult.FAILURE;
    }
}
```

### 对象消息监听器

```java
@Component
@RocketMQMessageListener(
    topic = "user-topic",
    consumerGroup = "user-group"
)
public class UserMessageListener extends LambdaRocketMQListener<User> {
    
    @Override
    public ConsumeResult consume(LambdaMessageView<User> messageView) {
        User user = messageView.getBody();
        System.out.println("Received user: " + user.getName());
        
        // 访问消息属性
        String source = messageView.getProperties().get("source");
        System.out.println("Message source: " + source);
        
        return ConsumeResult.SUCCESS;
    }
    
    @Override
    public ConsumeResult consumeError(MessageView messageView, Exception exception) {
        // 处理消费异常
        return ConsumeResult.FAILURE;
    }
}
```

### 基本类型消息监听器

```java
@Component
@RocketMQMessageListener(
    topic = "number-topic",
    consumerGroup = "number-group"
)
public class NumberMessageListener extends LambdaRocketMQListener<Integer> {
    
    @Override
    public ConsumeResult consume(LambdaMessageView<Integer> messageView) {
        Integer number = messageView.getBody();
        System.out.println("Received number: " + number);
        return ConsumeResult.SUCCESS;
    }
    
    @Override
    public ConsumeResult consumeError(MessageView messageView, Exception exception) {
        return ConsumeResult.FAILURE;
    }
}
```

## 配置示例

### RocketMQ v5 客户端基本配置

```yaml
rocketmq:
  # RocketMQ v5 使用 endpoints 替代传统的 name-server
  producer:
    endpoints: localhost:8081  # RocketMQ Broker 或 Proxy 地址
    # 多个地址使用分号分隔: localhost:8081;localhost:8082
  push-consumer:
    endpoints: localhost:8081  # 消费者连接地址
  simple-consumer:
    endpoints: localhost:8081  # 简单消费者连接地址
```

### 生产者配置

```yaml
rocketmq:
  producer:
    endpoints: localhost:8081
    # 可选配置
    request-timeout: 3s
    max-attempts: 3
    topics:
      - test-topic
      - user-topic
```

### 推送消费者配置

```yaml
rocketmq:
  push-consumer:
    endpoints: localhost:8081
    # 可选配置
    await-duration: 5s
    subscription-expressions:
      test-topic: "*"
      user-topic: "tag1 || tag2"
```

### 简单消费者配置

```yaml
rocketmq:
  simple-consumer:
    endpoints: localhost:8081
    consumer-group: simple-consumer-group
    # 可选配置
    await-duration: 5s
    subscription-expressions:
      test-topic: "*"
```

## 支持的数据类型

### 自动转换支持

1. **字符串类型** - 直接转换
2. **基本类型及包装类** - Integer, Long, Double, Boolean 等
3. **自定义对象** - 通过 Jackson 进行 JSON 序列化/反序列化

## 注意事项

- 继承 `LambdaRocketMQListener` 时需要指定泛型类型
- 确保消息体类型实现 `Serializable` 接口
- 消费异常时通过 `consumeError` 方法处理
- 返回 `ConsumeResult.SUCCESS` 表示消费成功
- 返回 `ConsumeResult.FAILURE` 表示消费失败，会触发重试
- 自定义对象需要提供无参构造函数用于反序列化