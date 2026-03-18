# lambda-cloud-starter-sse

`lambda-cloud-starter-sse` 提供基于 Spring MVC `SseEmitter` 的服务端推送能力，包含连接管理、单播/广播、心跳保活，以及可选的 Redisson 集群广播能力。

## 模块定位

- 提供开箱即用的 SSE 连接与消息推送组件。
- 支持本地单节点与 Redis 主题驱动的集群广播两种模式。
- 暴露可扩展接口，便于业务接入连接初始化和连接事件监听。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ SseAutoConfiguration.java
└─ SseProperties.java

src/main/java/com/lambda/cloud/sse/
├─ SseEmitterManager.java
├─ MessageType.java
├─ controller/SseController.java
├─ service/
│  ├─ SseService.java
│  └─ SseServiceImpl.java
├─ initializer/SseEmitterInitializer.java
├─ listener/
│  ├─ SseEventListener.java
│  └─ DefaultSseEventListener.java
├─ cluster/
│  ├─ ClusterSseEmitterManager.java
│  └─ ClusterMessage.java
└─ exception/SseException.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.SseAutoConfiguration
```

## 自动装配机制

`SseAutoConfiguration` 通过 `lambda.sse.cluster.enabled` 选择管理器实现：

- `false`（默认）→ `SseEmitterManager`（本地模式）
- `true` → `ClusterSseEmitterManager`（集群模式，依赖 `RedissonClient`）

其他自动装配 Bean：

- `SseService`（缺省时创建 `SseServiceImpl`）
- `SseController`（`lambda.sse.enable-endpoint=true` 时自动暴露）

## 配置模型

配置前缀：`lambda.sse`

- `timeout` 默认 `30000`
- `heartbeat-interval` 默认 `15000`
- `max-retry-attempts` 默认 `3`
- `enable-endpoint` 默认 `true`
- `enable-logging-listener` 默认 `true`
- `endpoint-prefix` 默认 `/sse`
- `subscribe-path` 默认 `/subscribe`
- `send-path` 默认 `/send`
- `broadcast-path` 默认 `/broadcast`

集群配置 `lambda.sse.cluster.*`：

- `enabled` 默认 `false`
- `channel-prefix` 默认 `sse:channel`
- `sync-timeout` 默认 `5000`
- `sync-heartbeat` 默认 `true`
- `node-id` 默认空（运行时自动生成 UUID）

最小配置示例：

```yaml
lambda:
  sse:
    timeout: 30000
    heartbeat-interval: 15000
    enable-endpoint: true
    endpoint-prefix: /sse
    subscribe-path: /subscribe
    send-path: /send
    broadcast-path: /broadcast
```

## HTTP 端点

默认端点由 `SseController` 提供：

- `GET  {endpointPrefix}{subscribePath}/{clientId}`
  - 创建连接并返回 `SseEmitter`
- `POST {endpointPrefix}{subscribePath}/{clientId}`
  - 携带初始化 payload 创建连接
- `POST {endpointPrefix}{sendPath}/{clientId}/{eventName}`
  - 单播推送
- `POST {endpointPrefix}{broadcastPath}/{eventName}`
  - 广播推送

按默认值展开即：

- `GET /sse/subscribe/{clientId}`
- `POST /sse/subscribe/{clientId}`
- `POST /sse/send/{clientId}/{eventName}`
- `POST /sse/broadcast/{eventName}`

## 推送链路

### 本地模式

1. `createEmitter(clientId)` 创建 `SseEmitter(timeout)`
2. 注册 `onCompletion/onTimeout/onError` 回调并自动移除连接
3. `sendEvent(...)` 异步单播，失败后移除连接
4. `broadcast(...)` 异步遍历连接逐个发送
5. 定时任务按 `heartbeatInterval` 广播 `heartbeat/ping`

### 集群模式

`ClusterSseEmitterManager` 在本地广播基础上增加：

- Redisson 主题订阅：`{channel-prefix}:broadcast`
- 广播时发布 `ClusterMessage(sourceNode, BROADCAST, eventName, data)`
- 收到其他节点消息后执行本地 `broadcast(...)`
- 为避免循环，忽略 `sourceNode == 当前节点` 的消息
- 集群层默认不转发 `heartbeat` 事件

## 扩展点

- `SseEmitterInitializer`
  - `initialize(emitter)`
  - `initialize(emitter, payload)`
  - 用于连接建立时下发欢迎消息、绑定上下文等
- `SseEventListener`
  - `onConnect(clientId)`
  - `onDisconnect(clientId)`
  - `onMessageSent(clientId, eventName)`
  - 可用于审计、在线状态同步等

## 统计与运维

`SseEmitterManager#getStatistics()` 返回：

- `activeConnections`
- `totalConnections`
- `totalMessagesSent`
- `failedMessages`
- `retryAttempts`
- `heartbeatInterval`

可通过 `getActiveClients()` 获取当前活跃 clientId 集合。

## 使用示例

```java
@RestController
@RequestMapping("/demo/sse")
public class DemoSseController {
    private final SseService sseService;

    public DemoSseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/subscribe/{clientId}")
    public SseEmitter subscribe(@PathVariable String clientId) {
        return sseService.createEmitter(clientId);
    }

    @PostMapping("/send/{clientId}")
    public void send(@PathVariable String clientId, @RequestBody Object payload) {
        sseService.sendEvent(clientId, "message", payload);
    }
}
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `com.lambda.cloud:lambda-cloud-starter-web`
- `com.lambda.cloud:lambda-cloud-core`
- `com.lambda.cloud:lambda-cloud-starter-redis`（optional，集群模式需要）

## 当前实现约束

- `maxRetryAttempts` 配置当前未在发送逻辑中生效，`retryAttempts` 统计也未递增。
- `enableLoggingListener` 配置未驱动默认监听器自动注册，需业务侧手工 `addEventListener(...)`。
- `sync-timeout` 与 `sync-heartbeat` 当前未参与集群转发逻辑控制。
- 集群广播会对 `data` 做 JSON 字符串化，远端节点收到后按字符串发送，不会自动反序列化为原对象。
- `connectionCount` 在重复 `clientId` 重连时会递增，但替换旧连接时不会先递减，统计值可能偏大。
- `SseServiceImpl#sendEvent` 抛出的是通用 `RuntimeException`，未使用模块内 `SseException`。
