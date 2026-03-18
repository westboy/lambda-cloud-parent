# lambda-cloud-starter-web

`lambda-cloud-starter-web` 是 Lambda Cloud 的 Web 基础 starter，围绕 Spring MVC 提供统一的 Web 自动配置、全局异常处理、请求上下文工具、过滤器和常用 HTTP 辅助能力。

## 模块定位

- 统一 Web 层基础行为，降低业务服务重复配置成本。
- 在 Spring Boot Web 默认能力之上，补充 Lambda Cloud 的异常模型与请求工具。
- 为安全、租户、日志等上层 starter 提供可复用的 Web 基础组件。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
└─ WebMvcAutoConfiguration.java

src/main/java/com/lambda/cloud/mvc/
├─ DeferredResultManager.java
├─ StringToDateConverter.java
├─ WebHttpUtils.java
├─ execption/
│  ├─ BusinessException.java
│  └─ GlobalControllerAdvice.java
├─ filter/
│  ├─ OrderedTimeHandlerFilter.java
│  └─ XframeOptionsFilter.java
└─ serializer/BigDecimalSerializer.java

src/main/java/com/lambda/cloud/web/
├─ AbstractEnvironmentPostProcessor.java
├─ LambdaHttpServletRequestWrapper.java
├─ LambdaServletInputStream.java
├─ RequestTimeHolder.java
└─ TenantHolder.java

src/main/resources/
├─ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
├─ error/401.html
├─ error/404.html
├─ error/5xx.html
└─ static/favicon.ico
```

自动装配注册项：

```text
com.lambda.autoconfig.WebMvcAutoConfiguration
```

## 自动装配机制

`WebMvcAutoConfiguration` 主要输出以下能力：

- `WebMvcConfigurer`
  - 注册 `StringToDateConverter`
  - 使用注入的 `LocalValidatorFactoryBean` 作为 MVC Validator
  - 按 `lambda.web.cors` 配置注册 CORS 规则
- `CorsProperty`（前缀 `lambda.web.cors`）
- `ObjectMapper`（Bean 名 `jacksonJsonMapper`，`@Primary`）
  - 默认 `NON_NULL`
  - 默认日期格式 `ExtendDateFormat`
  - 支持注入并合并 `JacksonModule` 扩展模块
- `LocaleResolver`
  - 默认 `Locale.SIMPLIFIED_CHINESE`
- `RestTemplate`（`@ConditionalOnMissingBean`）
- `OrderedTimeHandlerFilter`
- `GlobalControllerAdvice`
- `FilterRegistrationBean<XframeOptionsFilter>`
  - 仅匹配 `*.html`

可选 Thymeleaf 支持：

- 条件：类路径存在 `SpringTemplateEngine`
- 输出 `SpringResourceTemplateResolver`（`@Primary`）
- 复用 `ThymeleafProperties`，并通过反射兼容 `setCheckExistence(...)`

## Web 处理链路

### 请求进入

1. `OrderedTimeHandlerFilter` 最早执行（`order=Integer.MIN_VALUE`）。
2. 写入 `RequestTimeHolder` 的当前开始时间。
3. 请求结束后清理 ThreadLocal。

### 异常处理

`GlobalControllerAdvice` 将异常统一转为 `ErrorModel`：

- `400 BAD_REQUEST`
  - 缺参、参数绑定、JSON 反序列化、约束校验、上传大小超限
- `401 UNAUTHORIZED`
  - `SaTokenException`
- `403 FORBIDDEN`
  - `IllegalAccessException`（Lambda Cloud 自定义）
- `404 NOT_FOUND`
  - `NoResourceFoundException`
- `500 INTERNAL_SERVER_ERROR`
  - 通用异常 / 方法不支持 / 非法状态 / 不支持操作
- `501 NOT_IMPLEMENTED`
  - `BusinessException`
- `503 SERVICE_UNAVAILABLE`
  - `AbstractFeignException`

SSE 特殊处理：

- 若请求 `Accept` 包含 `text/event-stream`，500 处理分支返回 `null`，避免向 SSE 连接写常规错误体。

### 响应头安全

- `XframeOptionsFilter` 对 `*.html` 响应设置 `X-Frame-Options=sameorigin`。

## 核心组件说明

### WebHttpUtils

提供常用 HTTP 工具能力：

- 获取当前请求与请求属性
- 构建重定向 URL
- JWT Cookie 写入/清理
- 识别 Ajax 请求
- 识别 Bearer 请求和 HMAC 请求
- 解析表单参数和 JSON Body
- 组装完整请求 URL

关键常量：

- `Authorization`
- `Bearer `
- `HmacSHA `
- `x-authorized-token`
- `__redirectUrl`

### StringToDateConverter

- `String -> Date` 转换器，基于 `ExtendDateFormat` 解析多日期格式。
- 入参空白时返回 `null`。

### DeferredResultManager

- 基于 `Map<String, DeferredResult<T>>` 的简易异步结果管理器。
- 提供 `put/get/remove/setResult/size` 操作。

### TenantHolder / RequestTimeHolder

- 均基于 `ThreadLocal`。
- `TenantHolder` 保存当前线程租户 ID。
- `RequestTimeHolder` 保存当前请求开始时间。

### 请求体包装器

- `LambdaHttpServletRequestWrapper` 启动时缓存请求体字符串。
- `LambdaServletInputStream` 将缓存字符串重新暴露为可读输入流，支持后续多次读取。

### BigDecimalSerializer

- 序列化为 `toPlainString()`，用于保留尾部 0（如 `20.00`）。
- 典型用法：字段上使用 `@JsonSerialize(using = BigDecimalSerializer.class)`。

## 配置说明

目前模块内显式读取的配置前缀：

- `lambda.web.cors`

该配置最终绑定到 `CorsProperty`，常用项包含：

- `enabled`
- `allowedOrigins`
- `maxAge`

## 依赖说明

关键依赖（见 `pom.xml`）：

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-cloud-starter-bootstrap`
- `spring-boot-starter-thymeleaf`（optional）
- `com.lambda.cloud:lambda-cloud-core`
- `com.lambda.cloud:lambda-cloud-starter-logger`
- `com.lambda.cloud:lambda-cloud-starter-actuator`
- `cn.dev33:sa-token-core`

## 当前实现约束

- `XframeOptionsFilter` 只对 `*.html` 生效，API JSON 响应不会附加该响应头。
- `DeferredResultManager` 使用普通 `HashMap`，并发场景需业务侧自行保证线程安全。
- `TenantHolder` 与 `RequestTimeHolder` 都是 ThreadLocal，异步线程/线程复用场景必须注意显式清理。
- `GlobalControllerAdvice` 将 `BusinessException` 映射为 `501`，该语义偏“业务错误”而非“未实现”，需按网关契约评估。
- `WebHttpUtils#getRequestBody` 在解析失败时返回空 `JSONObject`，不会抛异常。
