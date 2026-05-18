---
name: "lambda-cloud-rpc"
description: "Lambda Cloud RPC通信配置指南。当用户需要配置Dubbo或Feign远程调用、认证透传、租户上下文传播或RPC监控时调用。"
---

# Lambda Cloud RPC 通信

## 模块引入

```xml
<!-- Dubbo RPC -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-dubbo</artifactId>
</dependency>

<!-- Feign HTTP 调用 -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-feign</artifactId>
</dependency>
```

## Dubbo 增强

### 配置

配置前缀: `lambda.dubbo`

```yaml
lambda:
  dubbo:
    security:
      enabled: true
      token-header: Authorization
      user-header: X-User-Id
      tenant-header: X-Tenant-Id
    monitoring:
      enabled: true
      enable-metrics: true
      enable-logging: true
      slow-call-threshold: 1000  # 毫秒
    retry:
      enabled: true
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2.0
      max-interval: 10000
      retryable-exceptions:
        - java.util.concurrent.TimeoutException
        - java.net.SocketTimeoutException
    tenant:
      enabled: false
      tenant-id-header: X-Tenant-Id
      default-tenant: default
      inherit-tenant-context: true
```

### 认证透传

`AuthenticationFilter` 自动从 RPC Context 读取 token/userId/tenantId:

```java
// Provider 端自动读取上游传递的认证信息
// Consumer 端自动向下游传递认证信息

// 在 Dubbo 服务中获取用户信息
String userId = DubboContextHolder.getCurrentUserId();
String tenantId = DubboContextHolder.getCurrentTenantId();
String traceId = DubboContextHolder.getCurrentTraceId();

// 检查是否有上下文信息
boolean hasCtx = DubboContextHolder.hasContext();

// 获取远程调用者地址和本地服务地址
String remoteAddr = DubboContextHolder.getRemoteAddress();
String localAddr = DubboContextHolder.getLocalAddress();

// 获取上下文描述字符串
String ctxInfo = DubboContextHolder.getContextInfo();
```

### 租户上下文

`TenantFilter` 自动管理租户上下文:

- Provider: 从 attachment 读取 tenantId, 写入 TenantContextHolder
- Consumer: 从 TenantContextHolder 读取 tenantId, 写入 attachment
- 无 tenant 时使用 `default-tenant` 配置值

```java
// 手动设置租户
DubboContextHolder.setCurrentTenantId("tenant001");

// 单独清理指定上下文
DubboContextHolder.clearCurrentTenantId();
DubboContextHolder.clearCurrentUserId();

// 清理所有上下文 (通常在 finally 中调用)
DubboContextHolder.clearContext();
```

### 调用监控

#### LoggingFilter

自动记录:
- 调用开始/结束
- 耗时
- 异常信息
- 慢调用告警 (超过 `slow-call-threshold`)

#### MetricsFilter + DubboMetricsCollector

收集指标:
- 总请求/成功/失败/慢请求
- 平均/最大/最小耗时
- 异常类型计数

#### DubboHealthIndicator

- 请求数 > 10 且成功率 < 95% 时标记不健康
- 任一服务不健康则整体 DOWN

### 重试策略

`DubboRetryInterceptor` 基于 Spring Retry:
- 策略: SimpleRetryPolicy + ExponentialBackOffPolicy
- 默认: 最大 3 次, 初始间隔 1s, 倍数 2.0, 最大间隔 10s
- 仅 Consumer 生效

## Feign 增强

### 配置

```yaml
spring:
  cloud:
    openfeign:
      client:
        base-package: com.your.company  # Feign 扫描包
        retry:
          enabled: true
          max-attempts: 3
      circuitbreaker:
        enabled: true
```

### 请求头透传

`AuthorizationRequestHeaderInterceptor` 自动处理:

1. 自动补 `Content-Type: application/json`
2. 存在 `x-security-policy` 时移除 `Authorization`
3. 尝试从当前请求 Header 或 Cookie 注入认证头

### 错误解码

`CustomErrorDecoder` 将 Feign 错误响应映射为 Lambda Cloud 异常:

| HTTP 状态码 | 异常类型 |
|-------------|----------|
| 400 | `FeignArgumentNotValidException` |
| 401 | `FeignUnauthorizedException` |
| 403 | `FeignAccessDeniedException` |
| 500 | `FeignInternalServerErrorException` |
| 503 | `FeignServiceNotAvailableException` |

### HMAC 签名

可选拦截器, 需手动注册:

```java
@Bean
public HmacClientRequestInterceptor hmacInterceptor(
        @Value("${hmac.appid}") String appid,
        @Value("${hmac.secret}") String secret) {
    return new HmacClientRequestInterceptor(appid, secret);
}
```

### 清理认证头

```java
@Bean
public ClearAuthorizationHeaderInterceptor clearAuthInterceptor() {
    return new ClearAuthorizationHeaderInterceptor();
}
```

### Feign 客户端示例

```java
@FeignClient(name = "user-service", path = "/api/users")
public interface UserFeignClient {

    @GetMapping("/{id}")
    UserVO getUserById(@PathVariable("id") Long id);

    @PostMapping
    UserVO createUser(@RequestBody UserCreateDTO dto);

    @GetMapping("/list")
    List<UserVO> listUsers(@RequestParam("status") String status);
}
```

### AttributeHolder 线程上下文

`AttributeHolder` 是实例方法（基于 `ThreadLocal<Map<String, String>>`），不是静态方法，需要通过注入获取实例:

```java
@Autowired
private AttributeHolder attributeHolder;

// 设置属性
attributeHolder.setAttribute("traceId", "xxx");

// 获取属性
String traceId = attributeHolder.getAttribute("traceId");

// 获取所有属性
Map<String, String> attrs = attributeHolder.getAttributes();

// 清理
attributeHolder.clear();
```

## Dubbo 与 Feign 选择

| 场景 | 推荐 | 理由 |
|------|------|------|
| 高性能内部调用 | Dubbo | 二进制协议, 性能更高 |
| 跨语言调用 | Feign | HTTP/REST, 兼容性好 |
| 服务网格集成 | Feign | 标准 HTTP, 易于接入 Sidecar |
| 已有 Dubbo 基础设施 | Dubbo | 复用现有注册中心和治理能力 |
| 简单的 HTTP 调用 | Feign | 声明式, 开发效率高 |

## 最佳实践

1. **认证透传**: 确保 `lambda.dubbo.security.enabled=true`
2. **租户隔离**: 微服务场景建议开启 `tenant.enabled=true`
3. **慢调用阈值**: 根据业务特点调整 `slow-call-threshold`
4. **重试次数**: 非幂等操作谨慎使用重试
5. **扫描包**: Feign 的 `base-package` 应包含所有 `@FeignClient` 接口
6. **超时配置**: Dubbo 和 Feign 都应合理设置超时时间
