# lambda-cloud-starter-gateway

`lambda-cloud-starter-gateway` 是基于 Spring Cloud Gateway 的网关增强模块，提供统一的全局过滤器、防火墙鉴权、CORS、路由扩展、Swagger 聚合与网关谓词扩展能力。

## 模块定位

- 提供 Gateway 层统一安全与请求预处理能力。
- 提供可扩展路由增强接口，支持业务模块动态追加路由。
- 提供 API 文档聚合输出，降低多服务文档接入成本。
- 兼容 Sa-Token 在 Reactor 场景下的统一认证校验。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
└─ GatewayAutoConfiguration.java

src/main/java/com/lambda/cloud/gateway/
├─ filter/
│  ├─ CorsWebFilter.java
│  ├─ ForwardAuthFilter.java
│  ├─ GlobalCacheRequestFilter.java
│  ├─ WebSocketExpandFilter.java
│  ├─ XFrameOptionsFilter.java
│  └─ factory/
│     ├─ BlackListUrlFilterFactory.java
│     └─ TenantRouteRewriterGatewayFilterFactory.java
├─ predicate/BackendRoutePredicateFactory.java
├─ properties/GatewayFirewallProperties.java
├─ service/
│  ├─ RouterEnhancer.java
│  └─ TenantRouteService.java
├─ swagger/
│  ├─ ConfigResource.java
│  └─ SwaggerResourceController.java
└─ utils/WebFluxUtils.java

src/main/resources/
├─ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
├─ org/springframework/http/mime.types
└─ static/error/{401.html,404.html,5xx.html}
```

自动装配注册项：

```text
com.lambda.autoconfig.GatewayAutoConfiguration
```

## 自动装配机制

`GatewayAutoConfiguration` 关键行为：

- `@AutoConfigureBefore(org.springframework.cloud.gateway.config.GatewayAutoConfiguration.class)`
- `@EnableConfigurationProperties(GatewayFirewallProperties.class)`

默认装配 Bean：

- `WebSocketClient`（`maxFramePayloadLength=524288`）
- `XFrameOptionsFilter`
- `GlobalCacheRequestFilter`
- `WebSocketExpandFilter("/ws/info")`
- `RouterFunction`：`GET /` 重定向到 `/index.html`
- `CorsProperty`（绑定 `lambda.web.cors`）
- `SaTokenConfig`（`@Primary`，绑定 `lambda.security.sa-token`）
- `RouteLocator`（汇总 `List<RouterEnhancer>`）
- `BackendRoutePredicateFactory`
- `NettyServerCustomizer`
- `SwaggerResourceController`

条件装配 Bean：

- `SwaggerDisabledConfigurer.routerFunction`
  - 条件：`lambda.api-docs.production=true`
  - 行为：`/doc.html`、`/v3/api-docs/**`、`/swagger-resources/**` 返回 404
- `ForwardAuthFilter`
  - 条件：存在 `lambda.security.sa-token.check-same-token` 配置
- `SaReactorFilter` + `ApplicationRunner`
  - 条件：`lambda.web.firewall.enabled=true`
- `CorsWebFilter`
  - 条件：`lambda.web.cors.enabled=true`

## 全局过滤链

### GlobalCacheRequestFilter

- 仅对 JSON 请求缓存 Body。
- 使用 `ServerWebExchangeUtils.cacheRequestBody(...)` 解决请求体多次读取问题。
- 顺序：`Ordered.HIGHEST_PRECEDENCE + 1`。

### ForwardAuthFilter

- Sa-Token same-token 开启时，为转发请求追加 `SaSameUtil.SAME_TOKEN` 请求头。
- 顺序：`-100`。

### WebSocketExpandFilter

- 当目标 URI scheme 为 `ws/wss` 且 path 为 `/ws/info` 时，改写为 `http/https`。
- 顺序：`Ordered.LOWEST_PRECEDENCE - 2`。

### XFrameOptionsFilter

- 为响应追加 `X-Frame-Options: SAMEORIGIN`。

### SaReactorFilter（防火墙模式）

- 匹配 `/**`，排除 `/favicon.ico`、`/actuator/**`。
- 对非白名单路径调用 `StpLogicUtils.getActiveStpLogic().checkLogin()`。
- 鉴权失败返回统一 JSON 错误模型（`ErrorModel`，HTTP 401）。

## GatewayFilterFactory 与 Predicate

### BlackListUrlFilterFactory

- 参数：`blacklistUrl`（支持 `**` 通配符）
- 命中黑名单后直接返回 401 与错误体。

### TenantRouteRewriterGatewayFilterFactory

- 从 Header 或 Query 读取租户参数（默认键 `tenantId`）。
- 依赖 `TenantRouteService.verify(...)` 校验租户有效性。
- 通过 `TenantRouteService.getUri(...)` 获取新 URI 后调用 `WebFluxUtils.rewriteExchangeUrl(...)` 改写路由。

### BackendRoutePredicateFactory

- 初始化默认排除：
  - `/static/**`
  - `/config.js`
  - `/index.html`
  - `/favicon.ico`
- 额外排除静态后缀：`*.html|*.css|*.js`
- 支持 `includes` 作为优先放行匹配集合（`GATHER_LIST`）。

## 路由扩展机制

`RouterEnhancer` 是扩展接口：

- `void config(RouteLocatorBuilder.Builder builder)`

自动配置会收集容器中的 `List<RouterEnhancer>`，统一参与 `RouteLocator` 构建，便于业务模块插拔式追加路由。

## Swagger 聚合机制

`SwaggerResourceController` 读取 `GatewayProperties.routes[*].metadata`：

- `api-docs`：文档 URL（必填）
- `api-name`：显示名称（可选，默认 routeId）
- `api-order`：排序号（可选，默认 `Integer.MAX_VALUE`）

输出地址：

- `${springdoc.api-docs.path:/v3/api-docs}/swagger-config`

输出结构由 `ConfigResource` 封装，字段 `urls` 对应聚合文档分组列表。

## 配置模型

### lambda.web.firewall

- `enabled`：默认 `false`
- `whites`：鉴权白名单路径
- `login-types`：Sa-Token 多登录体系初始化列表

### lambda.web.cors

由 `CorsProperty` 绑定，核心项：

- `enabled`
- `allowed-origins`
- `max-age`

### lambda.security.sa-token

- 网关中以 `@Primary` 方式注入 `SaTokenConfig`
- `check-same-token` 影响 `ForwardAuthFilter` 是否装配与执行

### lambda.api-docs.production

- `true` 时直接屏蔽 Swagger 文档入口（返回 404）

## 依赖说明

关键依赖（见 `pom.xml`）：

- `org.springframework.cloud:spring-cloud-starter-gateway-server-webflux`
- `org.springframework.cloud:spring-cloud-starter-loadbalancer`
- `cn.dev33:sa-token-reactor-spring-boot4-starter`
- `cn.dev33:sa-token-redis-jackson`
- `com.lambda.cloud:lambda-cloud-core`
- `com.lambda.cloud:lambda-cloud-starter-actuator`
- `com.lambda.cloud:lambda-cloud-starter-redis`（optional）

## 当前实现约束

- `TenantRouteRewriterGatewayFilterFactory` 未在本模块自动注册为 Bean，业务侧需自行声明并注入 `TenantRouteService`。
- `ForwardAuthFilter` 的装配条件仅判断配置存在，未要求值为 `true`，需注意配置语义。
- `WebSocketExpandFilter` 仅对固定路径 `/ws/info` 生效，其他 WebSocket 路径不会转换。
- `GlobalCacheRequestFilter` 只缓存 JSON 请求体，表单/其他 Content-Type 不覆盖。
- `BackendRoutePredicateFactory` 默认会拒绝 html/css/js 静态请求，需通过 includes 显式放行。
