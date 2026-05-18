---
name: "lambda-cloud-project-setup"
description: "Lambda Cloud 项目初始化与依赖配置指南。当用户需要创建新项目、引入 Lambda Cloud 依赖、配置 Starter 组合或设置多环境配置时调用。"
---

# Lambda Cloud 项目初始化

## BOM 引入

在业务工程的 `pom.xml` 中导入 BOM 统一管理版本:

```xml
<properties>
    <lambda-cloud.version>2026.1.1-SNAPSHOT</lambda-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.lambda.cloud</groupId>
            <artifactId>lambda-cloud-starter-dependencies</artifactId>
            <version>${lambda-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Starter 组合推荐

### Web 应用 (单体/微服务)

```xml
<dependencies>
    <!-- Web 基础 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-web</artifactId>
    </dependency>
    <!-- 安全认证 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-security</artifactId>
    </dependency>
    <!-- MyBatis 数据层 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-mybatis</artifactId>
    </dependency>
    <!-- Redis 缓存 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-redis</artifactId>
    </dependency>
    <!-- API 文档 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-swagger</artifactId>
    </dependency>
    <!-- 操作日志 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-logger</artifactId>
    </dependency>
    <!-- 监控 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

### 微服务网关

```xml
<dependencies>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-nacos</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-redis</artifactId>
    </dependency>
</dependencies>
```

### 微服务提供者 (Provider)

```xml
<dependencies>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-mybatis</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-dubbo</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-nacos</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-feign</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-swagger</artifactId>
    </dependency>
</dependencies>
```

### 注解处理器 (编译期)

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

## 环境要求

- **JDK**: 21 或更高版本
- **Maven**: 3.6+
- **构建工具**: 推荐 Maven，支持虚拟线程

## 基础配置模板

### application.yaml (Web 应用)

```yaml
spring:
  application:
    name: your-service-name
  datasource:
    url: jdbc:mysql://localhost:3306/your_db
    username: your-user
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
  data:
    redis:
      host: localhost
      port: 6379

mybatis-plus:
  mapper-package: com.your.company.**.mapper

lambda:
  web:
    cors:
      enabled: true
      allowed-origins: "*"
  security:
    sa-token:
      ignored:
        - /public/**
        - /v3/api-docs/**
    form:
      enabled: true
  api-docs:
    title: Your Service API
    enabled: true
  logging:
    operation:
      enabled: true
```

### application.yaml (微服务 + Nacos)

```yaml
spring:
  application:
    name: your-service-name
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
      config:
        server-addr: localhost:8848
        namespace: dev
        file-extension: yaml
```

## 多环境配置

建议使用 Spring Profile 机制:

```
src/main/resources/
├── application.yaml              # 公共配置
├── application-dev.yaml          # 开发环境
├── application-test.yaml         # 测试环境
├── application-prod.yaml         # 生产环境
└── bootstrap.yaml                # Nacos 配置中心引导
```

## 数据库版本管理

引入 Liquibase 自动执行数据库迁移:

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-liquibase</artifactId>
</dependency>
```

配置:
```yaml
lambda:
  liquibase:
    enabled: true
    url: jdbc:mysql://localhost:3306/your_db
    username: your-user
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

变更集文件命名约定: `lambda-xxx-changelog.xml`，放置在 `META-INF/db/changelogs/` 目录下。

## 关键注意事项

1. **不要自定义同名 Bean**: 部分 Starter 注册了默认 Bean (如 `stringRedisTemplate`)，自定义同名 Bean 会覆盖
2. **安全配置隔离**: Sa-Token 在 Gateway 中使用 `lambda.security.sa-token` 前缀，与业务侧 `sa-token.*` 隔离
3. **Mapper 扫描**: 配置 `mybatis-plus.mapper-package` 后自动注册 `MapperScannerConfigurer`
4. **CORS**: 通过 `lambda.web.cors` 配置，不要使用 Spring 原生 CORS 配置
5. **分页**: 使用 `BasePageDTO` 或 `PageRequest`，不要直接使用 MyBatis-Plus 的 `Page`
