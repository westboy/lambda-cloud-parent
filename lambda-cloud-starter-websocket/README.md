# lambda-cloud-starter-websocket

`lambda-cloud-starter-websocket` 提供基于 STOMP 的 WebSocket 能力，内置认证拦截、连接事件分发、订阅事件分发，以及可选的内存/Redis 在线会话存储。

## 模块定位

- 提供开箱即用的 STOMP + SockJS WebSocket 基础设施。
- 统一接入 Sa-Token 认证并将登录态注入 WebSocket 用户主体。
- 提供用户在线状态仓库，支持在线检测与会话管理。
- 提供连接/订阅事件扩展接口，便于业务插件式监听。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ WebSocketAutoConfiguration.java
└─ WebsocketProperties.java

src/main/java/com/lambda/cloud/websocket/
├─ session/StompWebSocketSession.java
├─ event/StompWebSocketSubscribeEvent.java
├─ handler/StompWebSocketEventHandler.java
├─ interceptor/
│  ├─ DefaultAuthenticationChannelInterceptor.java
│  └─ IpHandshakeInterceptor.java
├─ repository/
│  ├─ StompWebSocketChannelRepository.java
│  └─ impl/
│     ├─ DefaultStompWebSocketChannelRepository.java
│     └─ RedisStompWebSocketChannelRepository.java
└─ service/
   ├─ StompWebSocketConnectEventService.java
   └─ impl/DefaultStompWebSocketConnectEventServiceImpl.java

src/main/resources/
├─ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
└─ static/index.html
```

自动装配注册项：

```text
com.lambda.autoconfig.WebSocketAutoConfiguration
```

## 自动装配机制

`WebSocketAutoConfiguration` 生效条件：

- `lambda.websocket.enabled=true` 或缺省（`matchIfMissing = true`）

主要自动装配项：

- `StompWebSocketChannelRepository`
  - `channel-store-mode=REDIS` -> `RedisStompWebSocketChannelRepository`
  - 其他模式 -> `DefaultStompWebSocketChannelRepository`（默认 TTL 7 天）
- `StompWebSocketConnectEventService`
  - 默认实现：`DefaultStompWebSocketConnectEventServiceImpl`
- `ChannelInterceptor`
  - 默认：`DefaultAuthenticationChannelInterceptor`
- `StompWebSocketEventHandler`
  - 汇总所有连接事件服务与订阅事件处理器

## STOMP Broker 与端点约定

默认 Broker 配置：

- `setUserDestinationPrefix(userPrefix)`
- `enableSimpleBroker(topicPrefix, userPrefix)`
- `setApplicationDestinationPrefixes(appPrefix)`

默认端点：

- STOMP 端点：`/ws/stomp`
- 端点注册启用 SockJS，并附带：
  - `streamBytesLimit=524288`
  - `httpMessageCacheSize=1000`
  - `disconnectDelay=30000`
  - `sessionCookieNeeded=false`

握手拦截：

- `IpHandshakeInterceptor` 将客户端 IP 写入 session attribute（键：`ip`）。

## 认证链路

`DefaultAuthenticationChannelInterceptor` 在 `CONNECT` 命令处理：

1. 读取 `x-websocket-framework` 头并写入 session attributes。
2. 从 `Authorization` 头提取 `Bearer` Token。
3. 通过 `StpLogicUtils.getSaSession(token)` 获取登录会话。
4. 从会话读取 `loginUser` 并注入 `accessor.setUser(...)`。
5. 无 token 或无用户信息时抛 `AuthenticationException`。

说明：

- 当前实现强依赖 Sa-Token 会话中存在 `loginUser` 对象。

## 会话模型与事件分发

### StompWebSocketSession

统一封装事件上下文，提供：

- `sessionId`
- `topic`
- `user`
- `sessionAttributes`
- `ip`
- `framework`

### StompWebSocketEventHandler

监听并分发事件：

- `SessionConnectEvent`
- `SessionConnectedEvent`
- `SessionDisconnectEvent`
- `SessionSubscribeEvent`
- `SessionUnsubscribeEvent`

分发规则：

- 连接类事件广播给所有 `StompWebSocketConnectEventService`。
- 订阅类事件按 `topics()` 精确匹配分发给 `StompWebSocketSubscribeEvent`。

## 在线会话存储策略

### 默认内存模式（DEFAULT）

`DefaultStompWebSocketChannelRepository` 基于 Caffeine：

- key：`uid`
- value：`Set<sid>`
- 过期：`expireAfterWrite(timeout)`（默认 7 天）
- 支持在线用户统计与批量在线判断。

### Redis 模式（REDIS）

`RedisStompWebSocketChannelRepository` 关键键结构：

- 用户会话集合：`lambda:websocket:online_user:{tenant}:{uid}`
- 在线用户集合：`lambda:websocket:online_users:{tenant}`

特点：

- 通过 Lua 脚本维护用户集合与在线集合一致性。
- 自动拼接租户维度（`TenantHolder.getTenantId()`，缺省回退 `system`）。

## 连接事件默认行为

`DefaultStompWebSocketConnectEventServiceImpl`：

- `connectedEvent`：将 `uid/sid` 写入仓库。
- `disconnectEvent`：将 `uid/sid` 从仓库移除。
- 要求 `user != null` 且 `framework != null` 才执行仓库更新。

## 配置模型

配置前缀：`lambda.websocket`

- `enabled` 默认 `true`（设置为 `false` 可关闭自动装配）
- `channel-store-mode` 默认 `DEFAULT`
- `app-prefix` 默认 `/app`
- `user-prefix` 默认 `/user/`
- `topic-prefix` 默认 `/topic/`
- `stomp-endpoint` 默认 `/ws/stomp`
- `origin-endpoint` 默认 `/ws/native`
- `origins` 默认 `*`

示例：

```yaml
lambda:
  websocket:
    enabled: true
    channel-store-mode: REDIS
    app-prefix: /app
    user-prefix: /user/
    topic-prefix: /topic/
    stomp-endpoint: /ws/stomp
    origins: "*"
```

## 扩展点

- `StompWebSocketConnectEventService`
  - 扩展连接建立/断开生命周期处理
- `StompWebSocketSubscribeEvent`
  - 通过 `topics()` 声明订阅主题，处理订阅/取消订阅事件
- `StompWebSocketChannelRepository`
  - 可替换在线会话存储实现

## 依赖说明

关键依赖（见 `pom.xml`）：

- `spring-boot-starter-websocket`
- `com.lambda.cloud:lambda-cloud-starter-security`
- `com.lambda.cloud:lambda-cloud-starter-web`
- `com.lambda.cloud:lambda-cloud-starter-redis`
- `com.github.ben-manes.caffeine:caffeine`
- `com.lambda.cloud:lambda-cloud-core`

## 当前实现约束

- `WebSocketAutoConfiguration` 在 REDIS 模式通过 `SpringUtil.getBean(StringRedisTemplate.class)` 获取 Bean，若未提供会在运行期失败。
- `DefaultStompWebSocketConnectEventServiceImpl` 依赖 `framework` 非空，客户端未传 `x-websocket-framework` 时不会记录在线状态。
- `StompWebSocketSubscribeEvent` 的 topic 匹配为精确匹配，不支持 Ant 风格通配。
- `originEndpoint` 属性当前未在自动配置中使用，属于未接入配置项。
