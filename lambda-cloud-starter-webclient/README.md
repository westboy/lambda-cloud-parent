# Lambda Cloud WebClient Starter

WebClient模块，提供基于Spring WebFlux的响应式HTTP客户端支持。

## 核心功能

### 1. 自动配置
- 自动配置WebClient实例
- 支持多客户端配置
- 连接池管理
- 超时配置
- SSL/TLS支持

### 2. 请求拦截
- 日志记录拦截器
- 指标监控拦截器
- HMAC认证拦截器
- 重试机制拦截器

### 3. 负载均衡
- 支持多种负载均衡策略
- 健康检查
- 故障转移

### 4. 监控指标
- 请求响应时间
- 成功/失败率
- 并发连接数
- 错误统计

## 配置项

```yaml
lambda:
  webclient:
    enabled: true # 是否启用
    metrics-enabled: true # 是否启用指标监控
    logging-enabled: true # 是否启用请求日志
    
    # 默认配置
    default-config:
      base-url: http://localhost:8080
      connect-timeout: 10s
      read-timeout: 30s
      write-timeout: 30s
      response-timeout: 30s
      max-in-memory-size: 1048576 # 1MB
      
      # 连接池配置
      connection-pool:
        max-connections: 500
        max-idle-time: 30s
        max-life-time: 30m
        acquire-timeout: 45s
      
      # 重试配置
      retry:
        enabled: true
        max-attempts: 3
        backoff: 1s
        max-backoff: 10s
        multiplier: 2.0
        retryable-status-codes: [500, 502, 503, 504]
      
      # SSL配置
      ssl:
        enabled: false
        trust-all: false
        key-store: classpath:keystore.p12
        key-store-password: password
        trust-store: classpath:truststore.p12
        trust-store-password: password
      
      # HMAC认证配置
      hmac-enabled: false
      hmac:
        app-id: your-app-id
        secret: your-secret
        algorithm: HmacSHA256
        timestamp-tolerance: 300
    
    # 多客户端配置
    clients:
      user-service:
        base-url: http://user-service
        connect-timeout: 5s
        hmac-enabled: true
        hmac:
          app-id: user-client
          secret: user-secret
      
      order-service:
        base-url: http://order-service
        retry:
          max-attempts: 5
          backoff: 2s
    
    # 全局请求头
    default-headers:
      User-Agent: Lambda-WebClient/1.0
      Accept: application/json
```

## 使用说明

### 1. 添加依赖
```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-webclient</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 基本使用
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final WebClientService webClientService;
    
    // GET请求
    public Mono<User> getUser(Long id) {
        return webClientService.get("/users/" + id, User.class);
    }
    
    // POST请求
    public Mono<User> createUser(User user) {
        return webClientService.post("/users", user, User.class);
    }
    
    // 使用指定客户端
    public Mono<User> getUserFromService(Long id) {
        return webClientService.get("user-service", "/users/" + id, User.class);
    }
    
    // 获取Result包装的响应
    public Mono<Result<User>> getUserResult(Long id) {
        return webClientService.getResult("/users/" + id, User.class);
    }
    
    // 流式请求
    public Flux<User> getAllUsers() {
        return webClientService.getStream("/users", User.class);
    }
}
```

### 3. 自定义WebClient
```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient customWebClient(WebClientFactory factory) {
        WebClientProperties.ClientConfig config = new WebClientProperties.ClientConfig();
        config.setBaseUrl("http://custom-service");
        config.setConnectTimeout(Duration.ofSeconds(5));
        
        return factory.create("custom", config);
    }
}
```

### 4. 响应式编程
```java
@RestController
@RequiredArgsConstructor
public class UserController {
    
    private final WebClientService webClientService;
    
    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<User>> getUser(@PathVariable Long id) {
        return webClientService.get("/users/" + id, User.class)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/users")
    public Mono<ResponseEntity<User>> createUser(@RequestBody User user) {
        return webClientService.post("/users", user, User.class)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
```

## 高级特性

### 1. 错误处理
```java
public Mono<User> getUserWithErrorHandling(Long id) {
    return webClientService.get("/users/" + id, User.class)
            .onErrorResume(WebClientResponseException.class, ex -> {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Mono.empty();
                }
                return Mono.error(new ServiceException("获取用户失败"));
            })
            .timeout(Duration.ofSeconds(10))
            .retry(3);
}
```

### 2. 并行请求
```java
public Mono<UserProfile> getUserProfile(Long userId) {
    Mono<User> userMono = webClientService.get("/users/" + userId, User.class);
    Mono<List<Order>> ordersMono = webClientService.getStream("/users/" + userId + "/orders", Order.class)
            .collectList();
    
    return Mono.zip(userMono, ordersMono)
            .map(tuple -> new UserProfile(tuple.getT1(), tuple.getT2()));
}
```

### 3. 条件请求
```java
public Mono<User> updateUserIfExists(Long id, User user) {
    return webClientService.get("/users/" + id, User.class)
            .flatMap(existingUser -> 
                webClientService.put("/users/" + id, user, User.class))
            .switchIfEmpty(Mono.error(new UserNotFoundException()));
}
```

## 注意事项

1. 默认使用Reactor Netty作为HTTP客户端
2. 支持连接池复用，提高性能
3. 内置重试机制，提高可靠性
4. 支持指标监控，便于运维
5. 支持HMAC认证，保证安全性
6. 响应式编程模型，支持高并发