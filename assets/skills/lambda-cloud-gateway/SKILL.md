---
name: "lambda-cloud-gateway"
description: "Lambda Cloud 网关配置指南。当用户需要配置API网关路由、防火墙鉴权、CORS、租户路由改写或Swagger聚合时调用。"
---

# Lambda Cloud 网关配置

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-gateway</artifactId>
</dependency>
```

## 防火墙鉴权

配置前缀: `lambda.web.firewall`

```yaml
lambda:
  web:
    firewall:
      enabled: true
      whites:
        - /public/**
        - /actuator/**
        - /v3/api-docs/**
        - /swagger-resources/**
      login-types:
        - login
        - hmac
```

### 工作机制

`SaReactorFilter` 拦截所有请求:
- 匹配 `/**`, 排除 `/favicon.ico`、`/actuator/**`
- 非白名单路径调用 `StpLogicUtils.getActiveStpLogic().checkLogin()`
- 鉴权失败返回统一 JSON 错误模型 (HTTP 401)

## CORS 跨域配置

```yaml
lambda:
  web:
    cors:
      enabled: true
      allowed-origins:
        - http://localhost:3000
        - https://your-domain.com
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600
```

## 路由配置

### 基础路由

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
```

### 路由扩展机制

实现 `RouterEnhancer` 接口动态追加路由:

```java
@Component
public class CustomRouteEnhancer implements RouterEnhancer {

    @Override
    public void config(RouteLocatorBuilder.Builder builder) {
        builder.route("dynamic-route", r -> r
            .path("/api/dynamic/**")
            .filters(f -> f.stripPrefix(1))
            .uri("lb://dynamic-service"));
    }
}
```

## 全局过滤器

### GlobalCacheRequestFilter

- 顺序: `Ordered.HIGHEST_PRECEDENCE + 1`
- 功能: 缓存 JSON 请求 Body, 解决多次读取问题
- 仅对 `application/json` Content-Type 生效

### ForwardAuthFilter

- 顺序: `-100`
- 功能: 为转发请求追加 `SaSameUtil.SAME_TOKEN` 请求头
- 条件: 存在 `lambda.security.sa-token.check-same-token` 配置

### WebSocketExpandFilter

- 类型: `record`，接受 `path` 参数
- 顺序: `Ordered.LOWEST_PRECEDENCE - 2`
- 功能: 将指定路径的 `ws/wss` 协议转换为 `http/https`
- 对配置的 `path` 参数指定的路径生效

### XFrameOptionsFilter

- 功能: 为响应追加 `X-Frame-Options: SAMEORIGIN`

## GatewayFilterFactory

### BlackListUrlFilterFactory

黑名单过滤, 命中后返回 401:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: blocked-route
          uri: lb://service
          predicates:
            - Path=/api/blocked/**
          filters:
            - name: BlackListUrl
              args:
                blacklistUrl: /api/blocked/forbidden/**
```

### TenantRouteRewriterGatewayFilterFactory

租户路由改写, 从 Header 或 Query 读取租户参数并改写路由:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: tenant-route
          uri: lb://tenant-service
          predicates:
            - Path=/api/tenant/**
          filters:
            - name: TenantRouteRewriter
              args:
                tenantIdHeader: X-Tenant-Id
```

**注意**: 需手动注册 Bean:

```java
@Bean
public TenantRouteRewriterGatewayFilterFactory tenantRouteRewriter(
        TenantRouteService tenantRouteService) {
    return new TenantRouteRewriterGatewayFilterFactory(tenantRouteService);
}
```

## BackendRoutePredicateFactory

后端路由谓词, 用于区分前后端请求:

- 默认排除: `/static/**`、`/config.js`、`/index.html`、`/favicon.ico`
- 额外排除静态后缀: `*.html|*.css|*.js`
- 支持 `includes` 作为优先放行匹配集合

## Swagger 文档聚合

`SwaggerResourceController` 自动聚合下游服务文档:

### 路由元数据配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          metadata:
            api-docs: /v3/api-docs
            api-name: 用户服务
            api-order: 1
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          metadata:
            api-docs: /v3/api-docs
            api-name: 订单服务
            api-order: 2
```

### 生产环境禁用

```yaml
lambda:
  api-docs:
    production: true  # 禁用 Swagger 文档入口
```

## Sa-Token 网关配置

网关中使用独立的 SaTokenConfig:

```yaml
lambda:
  security:
    sa-token:
      token-name: Authorization
      timeout: 86400
      active-timeout: 1800
      is-concurrent: true
      is-share: false
      token-style: uuid
      check-same-token: true
      ignored:
        - /public/**
        - /actuator/**
```

## 完整配置示例

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
          metadata:
            api-docs: /v3/api-docs
            api-name: 用户服务
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
          metadata:
            api-docs: /v3/api-docs
            api-name: 订单服务

lambda:
  web:
    firewall:
      enabled: true
      whites:
        - /public/**
        - /actuator/**
    cors:
      enabled: true
      allowed-origins:
        - http://localhost:3000
  security:
    sa-token:
      timeout: 86400
      check-same-token: true
  api-docs:
    title: API Gateway
    enabled: true
```

## 最佳实践

1. **白名单配置**: 将公开接口加入 `firewall.whites`
2. **CORS**: 网关层统一配置, 下游服务无需重复配置
3. **路由前缀**: 使用 `StripPrefix` 统一去除前缀
4. **文档聚合**: 为每个路由配置 `metadata.api-docs`
5. **生产安全**: 生产环境禁用 Swagger (`api-docs.production: true`)
6. **限流**: 结合 Redis + RequestRateLimiter 实现限流
