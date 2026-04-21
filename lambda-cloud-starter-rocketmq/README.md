# lambda-cloud-starter-rocketmq

`lambda-cloud-starter-rocketmq` 基于 `rocketmq-v5-client-spring-boot-starter` 提供 RocketMQ v5 的增强封装，核心目标是统一消息体反序列化与消费处理模型。

## 模块定位

- 自动装配 `RocketMQMessageConverter`（默认使用 `LambdaRocketMQMessageConverter`），统一消息体转换规则。
- 提供类型安全的消费抽象：`LambdaRocketMQListener<T>` + `LambdaMessageView<T>`。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
└─ RocketMqAutoConfiguration.java

src/main/java/com/lambda/cloud/rocketmq/
├─ listener/LambdaRocketMQListener.java
├─ message/LambdaMessageView.java
└─ support/LambdaRocketMQMessageConverter.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.RocketMqAutoConfiguration
```

## 自动装配机制

[RocketMqAutoConfiguration](src/main/java/com/lambda/autoconfig/RocketMqAutoConfiguration.java) 默认注册：

- `RocketMQMessageConverter` -> `LambdaRocketMQMessageConverter`

本模块不额外创建 producer/consumer 实例，连接与消费模型由 `rocketmq-v5-client-spring-boot-starter` 按 `rocketmq.*` 配置完成。

## 快速开始

### 1）引入依赖

```xml
<dependency>
  <groupId>com.lambda.cloud</groupId>
  <artifactId>lambda-cloud-starter-rocketmq</artifactId>
</dependency>
```

### 2）最小配置（RocketMQ v5 endpoints）

```yaml
rocketmq:
  producer:
    endpoints: localhost:8081
  push-consumer:
    endpoints: localhost:8081
```

### 3）声明一个 Listener

```java
@Component
@RocketMQMessageListener(topic = "test-topic", consumerGroup = "test-group")
public class StringMessageListener extends LambdaRocketMQListener<String> {
    @Override
    public ConsumeResult consume(LambdaMessageView<String> messageView) {
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consumeError(MessageView messageView, Exception exception) {
        return ConsumeResult.FAILURE;
    }
}
```

## 核心组件

- `LambdaRocketMQListener<T extends Serializable>`：消费入口抽象，成功/失败分别走 `consume/consumeError`。
- `LambdaMessageView<T>`：包含 messageId/topic/tag/keys/properties 与已转换的 `body`。
- `LambdaRocketMQMessageConverter`：统一字符串/基本类型/JSON 对象的转换行为。

## 当前实现约束

- 消息泛型 `T` 需满足 `Serializable` 约束（接口定义要求）。
- 自定义对象反序列化依赖 Jackson，建议提供无参构造与标准 getter/setter。
