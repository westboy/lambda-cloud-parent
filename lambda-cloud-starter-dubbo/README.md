# lambda-cloud-starter-dubbo

`lambda-cloud-starter-dubbo` 是 Dubbo 增强 starter，围绕认证透传、租户上下文、调用日志、调用指标、健康检查与重试做统一自动配置。

## 模块定位

- 在 `dubbo-spring-boot-starter` 基础上补充企业级治理能力。
- 通过 `Filter` + `Actuator` + `ConfigurationProperties` 组合，提供默认可用、可配置关闭的增强能力。
- 不替代 Dubbo 原生配置；本模块只负责增强链路与可观测性。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ DubboAutoConfiguration.java
└─ DubboProperties.java

src/main/java/com/lambda/cloud/dubbo/
├─ authorize/
│  ├─ AuthenticationFilter.java
│  ├─ TenantFilter.java
│  └─ DubboContextHolder.java
├─ logging/
│  └─ LoggingFilter.java
├─ monitor/
│  ├─ MetricsFilter.java
│  └─ DubboMetricsCollector.java
├─ health/
│  └─ DubboHealthIndicator.java
└─ retry/
   └─ DubboRetryInterceptor.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.DubboAutoConfiguration
```

## 自动装配机制

### DubboAutoConfiguration

生效条件：

- 存在 `org.apache.dubbo.config.ApplicationConfig`
- 开启 `@EnableDubbo`
- 启用配置属性绑定 `DubboProperties`（前缀 `lambda.dubbo`）

按条件注册组件：

- `AuthenticationFilter`：`lambda.dubbo.security.enabled=true`（默认开启）
- `LoggingFilter`：`lambda.dubbo.monitoring.enable-logging=true`（默认开启）
- `TenantFilter`：`lambda.dubbo.tenant.enabled=true`（默认关闭）
- `DubboRetryInterceptor`：`lambda.dubbo.retry.enabled=true`（默认开启）
- `DubboMetricsCollector`：`lambda.dubbo.monitoring.enable-metrics=true`（默认开启）
- `DubboHealthIndicator`：`lambda.dubbo.monitoring.enabled=true` 且存在 Actuator `HealthIndicator`

## 配置模型

`DubboProperties` 包含 4 组配置：

- `security`
  - `enabled` 默认 `true`
  - `tokenHeader` 默认 `Authorization`
  - `userHeader` 默认 `X-User-Id`
  - `tenantHeader` 默认 `X-Tenant-Id`
- `monitoring`
  - `enabled` 默认 `true`
  - `enableMetrics` 默认 `true`
  - `enableLogging` 默认 `true`
  - `slowCallThreshold` 默认 `1000ms`
- `retry`
  - `enabled` 默认 `true`
  - `maxAttempts` 默认 `3`
  - `initialInterval` 默认 `1000ms`
  - `multiplier` 默认 `2.0`
  - `maxInterval` 默认 `10000ms`
  - `retryableExceptions` 默认超时相关异常
- `tenant`
  - `enabled` 默认 `false`
  - `tenantIdHeader` 默认 `X-Tenant-Id`
  - `defaultTenant` 默认 `default`
  - `inheritTenantContext` 默认 `true`

## 核心链路

### 认证与上下文链路

`AuthenticationFilter`（Provider + Consumer）：

- 从 `RpcContext.getServerAttachment()` 读取 token/userId/tenantId。
- token 写入 `RpcContext.getServerContext().setAttachment("auth.token", token)`。
- userId/tenantId 写入 `DubboContextHolder`。
- `finally` 中调用 `DubboContextHolder.clearContext()` 清理线程上下文。

`DubboContextHolder`：

- 统一维护 `tenantId/userId/traceId` 的读取、写入和清理。
- `setCurrentXxx` 同时写 serviceContext 与 clientAttachment，便于向下游传播。

### 租户链路

`TenantFilter`（Provider + Consumer）：

- 若 `inheritTenantContext=true` 且本地已有 tenant，则写入 `clientAttachment` 透传到下游。
- 若上游 attachment 有 tenant，则覆盖当前上下文。
- 若上下文与上游都无 tenant，则写入 `defaultTenant`。
- 调用结束后按条件清理租户上下文。

### 日志链路

`LoggingFilter`（Provider + Consumer）：

- 记录调用开始、结束、耗时、异常。
- 当耗时超过 `slowCallThreshold` 输出慢调用告警。
- 默认读取远端地址用于定位调用来源。

### 指标链路

`MetricsFilter`（Provider + Consumer）：

- 统计每次调用耗时、成功/失败、异常信息。
- 最终写入 `DubboMetricsCollector`。

`DubboMetricsCollector`：

- 以 `接口简名.方法名` 为 key 维护并发安全指标。
- 指标包含：
  - 总请求/成功/失败/慢请求
  - 平均/最大/最小耗时
  - 异常类型计数

### 健康检查链路

`DubboHealthIndicator`：

- 从 `DubboMetricsCollector` 聚合健康状态。
- 单服务判定规则：请求数 > 10 且成功率 < 95% 时标记不健康。
- 任一服务不健康则整体 `DOWN`，否则 `UP`。

### 重试链路

`DubboRetryInterceptor`（仅 Consumer）：

- 基于 `Spring Retry` 的 `RetryTemplate`。
- 策略为 `SimpleRetryPolicy + ExponentialBackOffPolicy`。
- 仅对 `retryableExceptions` 命中的异常类型重试。
- 非 `RpcException` 最终包装为 `RpcException` 抛出。

## 使用示例

```yaml
lambda:
  dubbo:
    security:
      enabled: true
      token-header: Authorization
      user-header: X-User-Id
      tenant-header: X-Tenant-Id
    tenant:
      enabled: true
      tenant-id-header: X-Tenant-Id
      default-tenant: default
      inherit-tenant-context: true
    monitoring:
      enabled: true
      enable-metrics: true
      enable-logging: true
      slow-call-threshold: 1000
    retry:
      enabled: true
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2.0
      max-interval: 10000
      retryable-exceptions:
        - java.util.concurrent.TimeoutException
        - java.net.SocketTimeoutException
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `org.apache.dubbo:dubbo-spring-boot-starter`
- `org.apache.dubbo:dubbo-filter-validation`
- `com.lambda.cloud:lambda-cloud-core`
- `org.springframework.retry:spring-retry`
- `org.springframework.boot:spring-boot-starter-actuator`

## 当前实现约束

- `AuthenticationFilter` 在 `finally` 总是清理上下文，业务若需异步延迟使用上下文，需要自行转存。
- `TenantFilter` 与 `AuthenticationFilter` 都可能操作 tenant 上下文，混合启用时应统一租户来源策略。
- `DubboHealthIndicator` 的健康阈值写在实现中（成功率 95%、样本量 10），当前未参数化。
- `DubboMetricsCollector#getErrorCounts` 返回可变结构，读取侧建议只读使用。
