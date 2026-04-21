# lambda-cloud-starter-iotdb

`lambda-cloud-starter-iotdb` 提供 Apache IoTDB 的自动装配能力，覆盖 Tree/Table 两种数据模型的连接池，以及可选的订阅消费（subscription）管理。

## 模块定位

- 面向业务侧的 IoTDB 连接池装配：Tree（`SessionPool`）与 Table（`ITableSessionPool`）。
- 提供声明式订阅：通过 `@IotDbSubscription` + `MessageHandler` 的组合，在应用启动后自动注册消费者。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ IotDbAutoConfiguration.java
└─ IotDbProperties.java

src/main/java/com/lambda/cloud/iotdb/
├─ annotation/IotDbSubscription.java
├─ handler/MessageHandler.java
├─ manager/IotDbConsumerManager.java
├─ IotDbConsumerContainer.java
└─ IotDbConsumerRegistrar.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.IotDbAutoConfiguration
```

## 自动装配机制

装配入口为 [IotDbAutoConfiguration](src/main/java/com/lambda/autoconfig/IotDbAutoConfiguration.java)，核心条件如下：

- Tree 模式连接池：当 `lambda.iotdb.tree-dialect=true` 时装配 `SessionPool`（`@ConditionalOnMissingBean`）。
- Table 模式连接池：当 `lambda.iotdb.table-dialect=true` 时装配 `ITableSessionPool`（`@ConditionalOnMissingBean`）。
- 订阅能力：当 `lambda.iotdb.enable-subscription=true` 时启用子配置 `IotDbSubscriptionConfiguration`：
  - `ISubscriptionTreeSession`（Tree + subscription）
  - `ISubscriptionTableSession`（Table + subscription）
  - `IotDbConsumerManager` / `IotDbConsumerRegistrar`（用于扫描与注册 `@IotDbSubscription`）

## 配置模型

配置前缀：`lambda.iotdb`（见 [IotDbProperties](src/main/java/com/lambda/autoconfig/IotDbProperties.java)）

常用配置项：

- `tree-dialect`：是否启用 Tree 连接池
- `table-dialect`：是否启用 Table 连接池
- `enable-subscription`：是否启用订阅
- `host` / `port`：IoTDB 地址（`node-urls` 为空时用于拼默认节点）
- `node-urls[]`：集群节点列表（可选）
- `user` / `password`
- `database`：Table 模式数据库名
- `max-size`：连接池大小（默认 10）
- `thrift-max-frame-size`：Table/Subscription builder 使用
- `base-package`：订阅扫描包（默认 `com.lambda.cloud.iotdb`）

## 快速开始

### 1）引入依赖

```xml
<dependency>
  <groupId>com.lambda.cloud</groupId>
  <artifactId>lambda-cloud-starter-iotdb</artifactId>
</dependency>
```

### 2）最小配置（Tree）

```yaml
lambda:
  iotdb:
    tree-dialect: true
    host: localhost
    port: 6667
    user: your-user
    password: your-password
    max-size: 10
```

### 3）最小配置（Table）

```yaml
lambda:
  iotdb:
    table-dialect: true
    host: localhost
    port: 6667
    user: your-user
    password: your-password
    database: test_db
    thrift-max-frame-size: 67108864
```

### 4）启用订阅（可选）

```yaml
lambda:
  iotdb:
    enable-subscription: true
    base-package: com.example
```

## 使用示例

### Tree 写入示例

```java
@Service
public class IotDbService {
    private final SessionPool sessionPool;

    public IotDbService(SessionPool sessionPool) {
        this.sessionPool = sessionPool;
    }
}
```

### 订阅处理器示例

```java
@IotDbSubscription(consumerId = "consumer1", topic = "test_topic", consumerGroupId = "group1")
public class MyMessageHandler implements MessageHandler {
    @Override
    public void handle(RowRecord record) {
    }
}
```

## 当前实现约束

- `node-urls` 未配置时，默认节点为 `${host}:${port}`（见 [IotDbProperties#getNodeUrls](src/main/java/com/lambda/autoconfig/IotDbProperties.java)）。
- Tree/Table 可同时启用，但需业务侧自行避免“同一场景重复注入”导致的混用问题。
- `enable-subscription=true` 仅开启订阅相关 Bean，仍需同时开启 Tree 或 Table（否则对应 subscription session 不会装配）。
