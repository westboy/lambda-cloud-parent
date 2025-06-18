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
- 消息发送重试机制
- 详细运行统计指标

✔️ **企业级支持**
- 线程安全设计
- 优雅的超时处理
- 可扩展的事件监听机制
- 自定义异常处理

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
    
    # 心跳间隔(毫秒)
    heartbeat-interval: 15000
    
    # 消息发送最大重试次数
    max-retry-attempts: 3
    
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
    
    @Override
    public void onMessageSent(String clientId, String eventName) {
        // 消息发送审计
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

### 获取详细统计信息

```java
@RestController
@RequestMapping("/admin/sse")
public class SseMonitorController {
    
    private final SseEmitterManager emitterManager;
    
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return emitterManager.getStatistics();
    }
}
```

### 统计信息示例

```json
{
  "activeConnections": 42,
  "totalConnections": 128,
  "totalMessagesSent": 1024,
  "failedMessages": 5,
  "retryAttempts": 8,
  "heartbeatInterval": 15000
}
```

## 异常处理

### 自定义异常处理

```java
@RestControllerAdvice
public class SseExceptionHandler {
    
    @ExceptionHandler(SseException.class)
    public ResponseEntity<Map<String, Object>> handleSseException(SseException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "SSE Operation Failed");
        body.put("message", ex.getMessage());
        
        if (ex.getClientId() != null) {
            body.put("clientId", ex.getClientId());
        }
        
        if (ex.getEventName() != null) {
            body.put("eventName", ex.getEventName());
        }
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
```

## 注意事项

1. **性能考量**
   - 建议单个实例连接数不超过5000
   - 高并发场景建议配合负载均衡使用
   - 合理设置心跳间隔(默认15秒)

2. **可靠性保障**
   - 默认启用3次消息发送重试
   - 建议实现自定义监听器处理失败场景
   - 监控统计指标及时发现异常

3. **浏览器兼容性**
   - 现代浏览器均支持SSE协议
   - 需要处理自动重连逻辑

4. **最佳实践**
   - 为每个客户端使用唯一ID
   - 合理设置心跳间隔
   - 及时处理断开事件释放资源
   - 实现异常处理逻辑

## 版本记录

| 版本 | 日期       | 说明                |
|------|------------|-------------------|
| 1.0  | 2023-08-01 | 初始版本发布         |
| 1.1  | 2023-09-15 | 增加配置化支持       |
| 1.2  | 2023-10-01 | 新增心跳和重试机制    |
