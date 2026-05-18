---
name: "lambda-cloud-websocket"
description: "Lambda Cloud WebSocket配置指南。当用户需要实现STOMP WebSocket通信、在线会话管理或实时推送时调用。"
---

# Lambda Cloud WebSocket

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-websocket</artifactId>
</dependency>
```

## 配置

```yaml
lambda:
  websocket:
    enabled: true  # 默认值
    channel-store-mode: DEFAULT  # DEFAULT 或 REDIS
    stomp-endpoint: /ws/stomp  # STOMP 端点
    origin-endpoint: /ws/native  # 原生 WebSocket 端点
    app-prefix: /app  # 应用目的地前缀
    user-prefix: /user/  # 用户目的地前缀
    topic-prefix: /topic/  # 主题前缀
    origins: "*"  # 跨域配置
```

## STOMP 端点

默认端点:
- STOMP 端点: `/ws/stomp`
- 启用 SockJS 支持

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
```

## 认证链路

`DefaultAuthenticationChannelInterceptor` 在 CONNECT 命令处理:

1. 读取 `x-websocket-framework` 头并写入 session attributes
2. 从 `Authorization` 头提取 Bearer Token
3. 通过 `StpLogicUtils.getSaSession(token)` 获取登录会话
4. 从会话读取 `loginUser` 并注入 `accessor.setUser()`
5. 无 token 或无用户信息时抛 `AuthenticationException`

**注意**: 强依赖 Sa-Token 会话中存在 `loginUser` 对象。

## 会话管理

### StompWebSocketChannelRepository

两种实现:
- `DefaultStompWebSocketChannelRepository`: 内存存储 (默认 TTL 7 天)
- `RedisStompWebSocketChannelRepository`: Redis 存储 (配置 `channel-store-mode=REDIS`)

```java
@Service
public class OnlineService {

    @Autowired
    private StompWebSocketChannelRepository channelRepository;

    // 获取在线用户数
    public long getOnlineCount() {
        return channelRepository.size();
    }

    // 判断用户是否在线
    public boolean isOnline(String userId) {
        return channelRepository.exist(userId);
    }

    // 获取在线用户列表
    public Set<String> getOnlineUsers() {
        return channelRepository.getOnlineUsers();
    }
}
```

## 消息推送

### 推送到指定用户

```java
@Controller
public class NotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 推送给指定用户
    public void sendToUser(String userId, String message) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
    }

    // 推送给所有订阅者
    public void sendToAll(String message) {
        messagingTemplate.convertAndSend("/topic/broadcast", message);
    }
}
```

### 客户端订阅

```javascript
const stompClient = Stomp.over(new SockJS('/ws/stomp'));

stompClient.connect({ Authorization: 'Bearer ' + token }, function(frame) {
    // 订阅个人消息
    stompClient.subscribe('/user/queue/notifications', function(message) {
        console.log('个人消息:', message.body);
    });

    // 订阅广播消息
    stompClient.subscribe('/topic/broadcast', function(message) {
        console.log('广播消息:', message.body);
    });
});
```

## 事件监听

### 连接事件

```java
@Component
public class CustomConnectEventService implements StompWebSocketConnectEventService {

    @Override
    public void onConnect(Principal principal, Message<byte[]> message) {
        String userId = principal.getName();
        log.info("用户连接: {}", userId);
    }

    @Override
    public void onDisconnect(Principal principal, Message<byte[]> message) {
        String userId = principal.getName();
        log.info("用户断开: {}", userId);
    }
}
```

### 订阅事件

```java
@Component
public class CustomSubscribeHandler implements StompWebSocketSubscribeEvent {

    @Override
    public void onSubscribe(Principal principal, String destination) {
        String userId = principal.getName();
        log.info("用户 {} 订阅: {}", userId, destination);
    }
}
```

## IP 记录

`IpHandshakeInterceptor` 自动将客户端 IP 写入 session attribute:

```java
// 获取客户端 IP
String ip = (String) session.getAttributes().get("ip");
```

### StompWebSocketChannelRepository 方法列表

| 方法 | 说明 |
|------|------|
| `add(uid, sid)` | 添加用户会话 |
| `remove(uid, sid)` | 删除指定用户会话 |
| `removeAll(uid)` | 删除用户所有会话 |
| `get(uid)` | 获取用户的会话集合 |
| `exist(uid)` | 判断用户是否在线 |
| `size()` | 获取在线用户数 |
| `getOnlineUsers()` | 获取所有在线用户 |
| `getOnlineUsers(uids)` | 批量检测用户是否在线 |

## 完整示例

```java
@RestController
@RequestMapping("/api/ws")
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StompWebSocketChannelRepository channelRepository;

    // 发送消息给指定用户
    @PostMapping("/send/{userId}")
    public Result<Void> sendToUser(@PathVariable String userId, @RequestBody String message) {
        if (!channelRepository.isOnline(userId)) {
            return Result.fail("用户不在线");
        }
        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", message);
        return Result.success();
    }

    // 广播消息
    @PostMapping("/broadcast")
    public Result<Void> broadcast(@RequestBody String message) {
        messagingTemplate.convertAndSend("/topic/broadcast", message);
        return Result.success();
    }

    // 获取在线用户数
    @GetMapping("/online-count")
    public Result<Long> getOnlineCount() {
        return Result.success(channelRepository.size());
    }
}
```

## 最佳实践

1. **认证**: 确保客户端连接时携带有效的 Sa-Token
2. **会话存储**: 多实例部署使用 Redis 存储 (`channel-store-mode=REDIS`)
3. **心跳**: 配置 SockJS 心跳保持连接活跃
4. **消息格式**: 使用 JSON 格式传输消息
5. **异常处理**: 监听连接断开事件, 清理相关资源
6. **安全**: 配置 CORS 限制允许的源
