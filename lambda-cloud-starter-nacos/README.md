# lambda-cloud-starter-nacos

`lambda-cloud-starter-nacos` 是 Nacos 相关依赖的聚合 starter，用于在 Lambda Cloud 体系中统一引入 Nacos 客户端、服务发现与配置中心依赖，并处理部分冲突依赖（如日志适配器）。

## 模块定位

- 提供依赖聚合：`nacos-client`、`spring-cloud-starter-alibaba-nacos-discovery`、`spring-cloud-starter-alibaba-nacos-config`。
- 本模块不提供额外的自动装配类，Nacos 能力由 Spring Cloud Alibaba 官方 starter 完成。

## 快速开始

### 1）引入依赖

```xml
<dependency>
  <groupId>com.lambda.cloud</groupId>
  <artifactId>lambda-cloud-starter-nacos</artifactId>
</dependency>
```

### 2）最小配置（服务发现）

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
```

### 3）最小配置（配置中心）

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        file-extension: yaml
```

## 依赖说明

本模块在 [pom.xml](pom.xml) 中聚合：

- `com.alibaba.nacos:nacos-client`（排除 nacos 日志适配器依赖）
- `com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery`
- `com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config`

## 安全建议

- 不要在仓库中明文提交 `access-key/secret-key`，建议通过环境变量或配置中心加密能力注入。
