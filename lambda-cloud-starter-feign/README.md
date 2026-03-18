# lambda-cloud-starter-feign

`lambda-cloud-starter-feign` 提供 Feign 统一自动配置，包含客户端扫描、请求头透传、错误解码与重试能力。

## 模块定位

- 统一项目内 Feign 客户端默认行为，减少重复配置。
- 提供标准化远程异常转换（映射为 `lambda-cloud-core` 的 Feign 异常模型）。
- 提供认证头透传与可选 HMAC 签名拦截能力。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ FeignAutoConfiguration.java
└─ ExtendFeignClientProperties.java

src/main/java/com/lambda/cloud/feign/
├─ codec/CustomErrorDecoder.java
├─ hmac/HmacClientRequestInterceptor.java
├─ interceptors/
│  ├─ AuthorizationRequestHeaderInterceptor.java
│  └─ ClearAuthorizationHeaderInterceptor.java
└─ webflux/AttributeHolder.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.FeignAutoConfiguration
```

## 自动装配机制

### FeignAutoConfiguration

核心行为：

- `@EnableFeignClients` 扫描包默认值为 `com.lambda.cloud`。
- 支持两种配置键：
  - `spring.cloud.openfeign.client.base-package`
  - `spring.cloud.openfeign.client.basePackage`
- 注册默认 Bean：
  - `Logger.Level`：`FULL`
  - `ErrorDecoder`：`CustomErrorDecoder`（仅当用户未自定义时生效）
  - `Contract`：`SpringMvcContract`
  - `Decoder`：`SpringDecoder`
  - `RequestInterceptor`：`AuthorizationRequestHeaderInterceptor`
  - `AttributeHolder`
  - `Dynamic Retryer`：按条件开启

### Retryer 条件

- 条件注解：`@ConditionalOnProperty(prefix="spring.cloud.openfeign.client.retry", name="enabled", matchIfMissing=true)`
- 默认重试参数：`maxAttempts=3`（来自 `ExtendFeignClientProperties.Retry`）。
- 实际重试器：`Retryer.Default(100ms, 1min, maxAttempts)`。

## 核心组件

### ExtendFeignClientProperties

配置前缀：`spring.cloud.openfeign.client`

扩展字段：

- `basePackage`：默认 `com.lambda.cloud`
- `retry.enabled`：默认 `false`
- `retry.maxAttempts`：默认 `3`
- `ssl.enabled/cert/password`：SSL 扩展配置对象（当前自动配置中未直接使用）

### AuthorizationRequestHeaderInterceptor

默认请求拦截器，行为如下：

- 如果请求没有 `Content-Type`，自动补 `application/json`。
- 认证头处理：
  - 若同时存在 `x-security-policy` 与 `Authorization`，会移除 `Authorization`。
  - 若两者都不存在，则尝试注入认证头：
    1. 读取当前请求 Header `Authorization`
    2. 读取 Cookie `x-authorized-token`，并拼接 `Bearer <token>`
- 顺序：`PriorityOrdered` 最小值（最高优先级）。

### HmacClientRequestInterceptor

可选拦截器（需要业务自行注册 Bean）：

- 基于 `appid + secret + timestamp + query + body` 生成 HMAC 签名。
- 覆盖请求中的 `Authorization` 头。
- 仅在 `POST/PUT` 且有 body 时将 body 纳入签名。

### ClearAuthorizationHeaderInterceptor

可选拦截器（默认未自动注册）：

- 每次请求清理 `Authorization`，用于强制禁止透传敏感头。

### CustomErrorDecoder

Feign 错误响应解码规则：

- 尝试将响应体解析为 `ErrorModel`，按状态码映射异常：
  - 400 -> `FeignArgumentNotValidException`
  - 401 -> `FeignUnauthorizedException`
  - 403 -> `FeignAccessDeniedException`
  - 503 -> `FeignServiceNotAvailableException`
  - 其他 -> `FeignInternalServerErrorException`
- JSON 解析失败或 IO 异常时，构造兜底 `ErrorModel` 并返回 `FeignInternalServerErrorException`。

### AttributeHolder

- `ThreadLocal<Map<String,String>>` 形式的属性容器。
- 提供 `set/get/getAll/clear` 方法。
- 适用于同线程上下文传递；不是 Reactor Context 透传方案。

## 配置示例

```yaml
spring:
  cloud:
    openfeign:
      client:
        base-package: com.lambda.cloud
        retry:
          enabled: true
          maxAttempts: 3
```

可选 HMAC 拦截器注册：

```java
@Bean
public HmacClientRequestInterceptor hmacClientRequestInterceptor(
        @Value("${hmac.appid}") String appid,
        @Value("${hmac.secret}") String secret) {
    return new HmacClientRequestInterceptor(appid, secret);
}
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `spring-cloud-starter-openfeign`
- `spring-cloud-starter-loadbalancer`
- `feign-okhttp`
- `spring-retry`
- `lambda-cloud-core`
- `lambda-cloud-starter-logger`

## 当前实现约束

- `retry.enabled` 在属性类默认值为 `false`，但 `Retryer` Bean 的条件为 `matchIfMissing=true`，未配置时仍会注册默认重试器。
- `ssl` 扩展配置当前未在自动配置中落地到客户端构建流程。
- `AttributeHolder` 基于 `ThreadLocal`，跨线程/异步链路需业务自行处理清理与传递。
