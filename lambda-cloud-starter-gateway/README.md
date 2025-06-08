# lambda-cloud-starter-gateway 模块说明

## 模块概述
基于Spring Cloud Gateway的增强网关模块，提供企业级API网关能力。

## 核心功能

### 1. 请求处理增强
- **GlobalCacheRequestFilter**: 解决请求body不能重复读取问题，支持JSON请求缓存
- **WebSocketExpandFilter**: WebSocket协议转换(ws->http, wss->https)
- **XFrameOptionsFilter**: 添加X-Frame-Options响应头防止点击劫持

### 2. 路由增强
- **TenantRouteRewriterGatewayFilterFactory**: 基于租户ID的路由重写
  - 支持header/query参数获取tenantId
  - 依赖TenantRouteService验证租户路由
- **TenantRouteService**: 租户路由服务接口
  - `getUri()`: 根据租户ID获取新URI
  - `verify()`: 验证租户ID有效性

### 3. 安全防护
- **BlackListUrlFilterFactory**: URL黑名单过滤
  - 支持通配符模式匹配
  - 匹配失败返回401状态码
- **GatewayFirewallProperties**: 防火墙配置
  - 开关控制(enabled)
  - 白名单配置(whites)

### 4. Swagger支持
- **SwaggerResourceController**: 聚合各服务Swagger文档
  - 支持自定义API名称(api-name)
  - 支持自定义文档路径(api-docs)
  - 支持API分组排序(api-order)

## 配置说明

### 防火墙配置
```yaml
lambda:
  web:
    firewall:
      enabled: true
      whites:
        - /api/public/**
        - /health
```

### 租户路由配置
需实现TenantRouteService接口并注册为Spring Bean。

## 使用示例

### 黑名单过滤器配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: demo-service
          uri: lb://demo-service
          filters:
            - name: BlackListUrl
              args:
                patterns: /api/forbidden/**,/admin/secret
```

## 注意事项
1. TenantRouteService需要业务方自行实现
2. 防火墙默认禁用，需手动开启
3. Swagger聚合依赖各服务的Swagger配置
