# lambda-cloud-starter-webclient

`lambda-cloud-starter-webclient` 提供基于 Spring WebFlux `WebClient` 的统一 HTTP 客户端能力，包含多客户端配置、连接池/超时治理、认证/HMAC/重试/日志/指标过滤器链，以及面向业务的 `WebClientTemplate` 调用封装。

## 模块定位

- 统一创建与管理多实例 `WebClient`。
- 通过属性化配置控制超时、连接池、默认请求头和客户端特性。
- 提供可插拔过滤器链，支持鉴权、HMAC、日志、指标与重试。
- 通过 `WebClientTemplate` 提供常用 GET/POST/PUT/DELETE/流式调用入口。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ WebClientAutoConfiguration.java
└─ WebClientProperties.java

src/main/java/com/lambda/cloud/webclient/
├─ WebClientTemplate.java
├─ WebClientTemplateFactory.java
├─ authorization/AuthorizationExchangeFilterFunction.java
├─ hmac/HmacExchangeFilterFunction.java
├─ logging/LoggingExchangeFilterFunction.java
├─ metrics/MetricsExchangeFilterFunction.java
└─ retry/RetryExchangeFilterFunction.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.WebClientAutoConfiguration
```

## 自动装配机制

`WebClientAutoConfiguration` 生效条件：

- `@ConditionalOnClass(WebClient.class)`
- `@ConditionalOnProperty(prefix = "lambda.webclient", name = "enabled", matchIfMissing = true)`
- 总开关：`lambda.webclient.enabled`（缺省视为 `true`）

自动装配 Bean：

- 原型过滤器：
  - `LoggingExchangeFilterFunction`
  - `MetricsExchangeFilterFunction`（仅类路径存在 `MeterRegistry`）
  - `HmacExchangeFilterFunction`
  - `RetryExchangeFilterFunction`
- 单例过滤器：
  - `AuthorizationExchangeFilterFunction`
- 核心服务：
  - `WebClientTemplateFactory`
  - `WebClientTemplate`
  - `defaultWebClient`（Bean 名）

## 配置模型

配置前缀：`lambda.webclient`

### 全局配置

- `enabled` 默认 `true`
- `metrics-enabled` 默认 `true`
- `logging-enabled` 默认 `true`
- `default-headers` 全局默认请求头
- `default-config` 默认客户端配置
- `clients` 命名客户端配置映射

### ClientConfig（default-config / clients.*）

- `base-url`
- `authorization-enabled` 默认 `true`
- `connect-timeout` 默认 `10s`
- `read-timeout` 默认 `30s`
- `write-timeout` 默认 `30s`
- `response-timeout` 默认 `30s`
- `max-in-memory-size` 默认 `1048576`
- `headers` 客户端默认请求头
- `connection-pool.*`
- `retry.*`
- `ssl.*`
- `hmac.*`

### 子配置默认值

- `connection-pool.max-connections=500`
- `connection-pool.max-idle-time=30s`
- `connection-pool.max-life-time=30m`
- `connection-pool.acquire-timeout=45s`
- `retry.enabled=true`
- `retry.max-attempts=3`
- `retry.backoff=1s`
- `retry.max-backoff=10s`
- `retry.multiplier=2.0`
- `retry.retryable-status-codes=[500,502,503,504]`
- `ssl.enabled=false`
- `ssl.trust-all=false`
- `hmac.enabled=false`

示例：

```yaml
lambda:
  webclient:
    enabled: true
    logging-enabled: true
    metrics-enabled: true
    default-headers:
      User-Agent: Lambda-WebClient/1.0
    default-config:
      base-url: http://localhost:8080
      connect-timeout: 10s
      retry:
        enabled: true
        max-attempts: 3
    clients:
      user-service:
        base-url: http://user-service
        hmac:
          enabled: true
          app-id: user-client
          secret: user-secret
```

## 构建与调用链路

### WebClientTemplateFactory

工厂核心流程：

1. 按客户端名解析 `ClientConfig`（缺省回退 `defaultConfig`）
2. 基于 `ConnectionProvider` 创建连接池
3. 基于 Reactor Netty 创建 `HttpClient`，注入连接/响应/读写超时
4. 应用 `baseUrl`、全局 headers、客户端 headers
5. 按配置装配过滤器链后 `build()`

SSL 行为：

- `ssl.enabled=true` 时启用 TLS
- `ssl.trust-all=true` 时使用 `InsecureTrustManagerFactory`
- keystore/truststore 字段当前未落地加载

### WebClientTemplate

对业务暴露统一方法：

- `get(...)`
- `post(...)`（JSON / form）
- `put(...)`
- `delete(...)`
- `exchange(...)`
- `getStream(...)`
- `getWithTimeout(...)`

默认客户端名为 `default`，也可显式传入 `clientName`。

## 过滤器行为

### AuthorizationExchangeFilterFunction

- 从 Sa-Token 上下文读取活跃 `StpLogic` 与 `SaSession`。
- 若可用则自动注入 token 头：
  - header 名：`stpLogic.getTokenName()`
  - 值：`tokenPrefix + " " + token`
- 异常场景降级为透传原请求。

### HmacExchangeFilterFunction

- 对 `POST/PUT/PATCH` 请求尝试从 `request.attribute("requestData")` 读取签名体。
- 合并 query 参数与 body，生成 HMAC base string。
- 设置 `Authorization` 头为 HMAC 鉴权值。

### RetryExchangeFilterFunction

- 使用 `Retry.backoff(...)` 策略。
- 仅对 `WebClientResponseException` 且状态码命中 `retryableStatusCodes` 重试。

### LoggingExchangeFilterFunction

- DEBUG 级别输出请求/响应头、耗时。
- 异常时 ERROR 级别输出方法、URL、耗时与错误消息。

### MetricsExchangeFilterFunction

- 指标名：`webclient.requests`
- tags：`client`、`method`、`status`、`outcome`
- 请求结束或异常时停止计时并写入指标。

## 依赖说明

关键依赖（见 `pom.xml`）：

- `spring-boot-starter-webflux`
- `spring-webflux`
- `spring-cloud-starter-loadbalancer`
- `spring-retry`
- `com.lambda.cloud:lambda-cloud-core`
- `com.lambda.cloud:lambda-cloud-starter-logger`（optional）
- `com.lambda.cloud:lambda-cloud-starter-actuator`（optional）

## 当前实现约束

- `AuthorizationEnabled` 字段以大写 A 命名，配置映射需使用 `authorization-enabled`，可读性较弱。
- `HmacExchangeFilterFunction` 依赖请求属性 `requestData`，GET 请求与未设置属性的请求体不会参与签名。
- `RetryExchangeFilterFunction` 仅按响应状态码重试，不覆盖连接超时、DNS、IO 异常等网络错误。
- `WebClientTemplateFactory` 依赖 `SpringUtil.getBean(...)` 获取过滤器，脱离 Spring 容器时不可用。
- `ssl.key-store/trust-store` 等字段目前未真正加载，只有 `trust-all` 行为生效。
- 每次 `create(clientName)` 都重新构建 `WebClient` 与连接池，未做命名客户端缓存复用。
