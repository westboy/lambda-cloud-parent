# Lambda Cloud WebSocket Starter

WebSocket模块，提供基于STOMP协议的WebSocket支持。

## 核心功能

### 1. 自动配置
- 自动配置WebSocket消息代理
- 支持STOMP协议
- 支持SockJS
- 可配置的消息前缀和端点

### 2. 认证拦截
- 基于SaToken的认证拦截器
- 支持从Header获取accessToken
- 支持多种登录类型(ADMIN/USER)

### 3. 通道存储
- 提供内存和Redis两种存储模式
- 支持用户会话管理
- 支持在线用户统计
- 支持批量检测用户在线状态

### 4. 事件处理
- 处理连接生命周期事件（连接、断开）
- 处理订阅/取消订阅事件
- 支持自定义事件处理器

## 配置项

```yaml
lambda:
  websocket:
    enabled: true # 是否启用
    store-mode: memory # 存储模式(memory/redis)
    endpoint: /ws # WebSocket端点路径
    application-destination-prefix: /app # 应用目标前缀
    user-destination-prefix: /user # 用户目标前缀
    topic-prefix: /topic # 主题前缀
```

## 使用说明

### 1. 添加依赖
```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-websocket</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 自定义事件处理器
```java
@Component
public class CustomConnectEventService implements WsConnectEventService {
    @Override
    public void connectEvent(WsSessionInfo<SessionConnectEvent> info) {
        // 处理连接事件
    }
}

@Component
public class CustomSubscribeEvent implements WsSubscribeEvent {
    @Override
    public String[] topics() {
        return new String[]{"/topic/demo"};
    }

    @Override
    public void subscribeEvent(WsSessionInfo<SessionSubscribeEvent> info) {
        // 处理订阅事件
    }
}
```

### 3. 发送消息示例
```java
@RestController
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/send")
    public void sendMessage() {
        // 发送给特定用户
        messagingTemplate.convertAndSendToUser("userId", "/queue/messages", "Hello");
        
        // 广播消息
        messagingTemplate.convertAndSend("/topic/broadcast", "Broadcast message");
    }
}
```

## 注意事项
1. 默认使用内存存储模式，生产环境建议使用Redis模式
2. 认证拦截器需要配合SaToken使用
3. 订阅事件处理器需要实现WsSubscribeEvent接口并注册为Spring Bean
