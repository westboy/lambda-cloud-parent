# Lambda Cloud SSE Starter

## 概述

基于Spring Boot的Server-Sent Events(SSE)功能集成starter，提供轻量级的服务端推送能力。

## 功能特性

✔️ **核心功能**
- 客户端连接管理
- 单播消息推送
- 广播消息推送
- 自动心跳保持

✔️ **增强功能**
- 可配置的REST端点
- 连接生命周期监听
- 内置日志监听器
- 连接数统计监控

✔️ **企业级支持**
- 线程安全设计
- 优雅的超时处理
- 可扩展的事件监听机制

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-sse</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基础使用

```java
@RestController
public class SseExampleController {
    
    private final SseEmitterManager emitterManager;
    
    // 1. 订阅接口
    @GetMapping("/events")
    public SseEmitter subscribe(@RequestParam String clientId) {
        return emitterManager.createEmitter(clientId);
    }
    
    // 2. 消息推送接口
    @PostMapping("/events/push")
    public void pushEvent(@RequestParam String clientId, 
                        @RequestBody String message) {
        emitterManager.sendEvent(clientId, "message", message);
    }
}
```

## 详细配置

```yaml
lambda:
  sse:
    # 连接超时(毫秒)
    timeout: 30000
    
    # 是否启用自动配置的Controller
    enable-controller: true
    
    # 是否启用日志监听器
    enable-logging-listener: true
    
    # 端点配置
    endpoint-prefix: /sse      # 基础路径
    subscribe-path: /connect   # 订阅路径
    send-path: /send          # 单播路径
    broadcast-path: /broadcast # 广播路径
```

## 高级用法

### 自定义监听器

```java
@Component
@Order(0) // 监听器执行顺序
public class AuditEventListener implements SseEventListener {
    
    @Override
    public void onConnect(String clientId) {
        // 审计日志记录
    }
    
    @Override
    public void onDisconnect(String clientId) {
        // 资源清理
    }
}
```

### 自定义配置

```java
@Configuration
public class CustomSseConfig {
    
    @Bean
    public SseEmitterManager customEmitterManager(SseProperties properties) {
        // 自定义实现
        return new CustomSseEmitterManager(properties);
    }
}
```

## 监控管理

### 监控端点

```java
@RestController
@RequestMapping("/admin/sse")
public class SseMonitorController {
    
    private final SseEmitterManager emitterManager;
    
    // 获取活跃连接数
    @GetMapping("/connections/count")
    public int getConnectionCount() {
        return emitterManager.getActiveConnectionCount();
    }
    
    // 获取连接列表
    @GetMapping("/connections")
    public List<String> getConnections() {
        return emitterManager.getActiveClients();
    }
}
```

## 注意事项

1. **性能考量**
   - 建议单个实例连接数不超过5000
   - 高并发场景建议配合负载均衡使用

2. **浏览器兼容性**
   - 现代浏览器均支持SSE协议
   - 需要处理自动重连逻辑

3. **最佳实践**
   - 为每个客户端使用唯一ID
   - 合理设置心跳间隔
   - 及时处理断开事件释放资源
