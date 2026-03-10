# lambda-cloud-starter-dependencies 依赖管理模块

## 项目概览

- **版本**: 2026.1.1-SNAPSHOT
- **Java 版本**: 21
- **Spring Boot 版本**: 3.5.3
- **Spring Cloud 版本**: 2025.0.0

## 功能概述

本模块是 Lambda Cloud 项目的依赖管理父模块 (BOM - Bill of Materials)，主要功能：

1. 统一管理所有 starter 模块的版本
2. 提供 dependencyManagement 供子模块继承
3. 配置项目发布到 Maven 私服

## 管理的模块

### 核心模块

| 模块名称 | ArtifactId | 说明 |
|---------|------------|------|
| lambda-cloud-core | lambda-cloud-core | 核心模块，提供基础模型、工具类和异常处理 |
| lambda-cloud-processor | lambda-cloud-processor | 编译时注解处理器，用于生成 MapStruct 转换器 |

### Starter 模块 (共 26 个)

| 模块名称 | ArtifactId | 说明 |
|---------|------------|------|
| lambda-cloud-starter-actuator | lambda-cloud-starter-actuator | 监控增强模块，提供指标收集和资源解析 |
| lambda-cloud-starter-cache | lambda-cloud-starter-cache | 统一缓存抽象层，支持 Redis/Caffeine/多级缓存 |
| lambda-cloud-starter-datasource | lambda-cloud-starter-datasource | 数据源模块，支持标准/动态多数据源 |
| lambda-cloud-starter-dubbo | lambda-cloud-starter-dubbo | Dubbo RPC 框架集成 |
| lambda-cloud-starter-feign | lambda-cloud-starter-feign | Feign 客户端模块 |
| lambda-cloud-starter-gateway | lambda-cloud-starter-gateway | API 网关模块 |
| lambda-cloud-starter-iotdb | lambda-cloud-starter-iotdb | IoTDB 时序数据库集成 |
| lambda-cloud-starter-kafka | lambda-cloud-starter-kafka | Kafka 消息队列集成 |
| lambda-cloud-starter-liquibase | lambda-cloud-starter-liquibase | 数据库版本管理 |
| lambda-cloud-starter-logger | lambda-cloud-starter-logger | 操作日志模块 |
| lambda-cloud-starter-mybatis | lambda-cloud-starter-mybatis | MyBatis-Plus 集成 |
| lambda-cloud-starter-nacos | lambda-cloud-starter-nacos | Nacos 服务发现和配置管理 |
| lambda-cloud-starter-netty | lambda-cloud-starter-netty | Netty 网络通信模块 |
| lambda-cloud-starter-oss | lambda-cloud-starter-oss | 对象存储模块 (MinIO/阿里云/腾讯云) |
| lambda-cloud-starter-redis | lambda-cloud-starter-redis | Redis 集成 (含 Redisson) |
| lambda-cloud-starter-rocketmq | lambda-cloud-starter-rocketmq | RocketMQ 消息队列集成 |
| lambda-cloud-starter-security | lambda-cloud-starter-security | 安全认证模块 (Sa-Token) |
| lambda-cloud-starter-sms | lambda-cloud-starter-sms | 短信模块 (阿里云/腾讯云) |
| lambda-cloud-starter-sse | lambda-cloud-starter-sse | Server-Sent Events 推送 |
| lambda-cloud-starter-swagger | lambda-cloud-starter-swagger | API 文档模块 (Knife4j) |
| lambda-cloud-starter-test | lambda-cloud-starter-test | 测试模块 |
| lambda-cloud-starter-web | lambda-cloud-starter-web | Web 基础模块 |
| lambda-cloud-starter-webclient | lambda-cloud-starter-webclient | WebClient 响应式客户端 |
| lambda-cloud-starter-websocket | lambda-cloud-starter-websocket | WebSocket 模块 |
| lambda-cloud-starter-ykc | lambda-cloud-starter-ykc | YKC 充电桩协议模块 |

## 核心依赖版本

| 依赖名称 | 版本 |
|---------|------|
| Spring Boot | 3.5.3 |
| Spring Cloud | 2025.0.0 |
| Spring Cloud Alibaba | 2025.0.0.0 |
| MyBatis-Plus | 3.5.12 |
| Sa-Token | 1.43.0 |
| Dubbo | 3.3.5 |
| RocketMQ | 2.3.4 |
| IoTDB | 2.0.3 |
| Redisson | 3.34.1 |
| MapStruct | 1.6.0.RC1 |

## 发布配置

配置了发布到私有 Maven 仓库：

```xml
<distributionManagement>
    <repository>
        <id>maven-releases</id>
        <url>http://192.168.130.243:8081/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>maven-snapshots</id>
        <url>http://192.168.130.243:8081/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

## Lombok 配置

配置了 Lombok 生成的注解标记：

```
lombok.addLombokGeneratedAnnotation=true
```

## 使用说明

1. 子模块继承本模块即可统一版本
2. 发布时使用 `mvn deploy` 命令
3. 版本号继承自父项目 `${project.parent.version}`

## 模块依赖关系图

```
lambda-cloud-core (核心基础)
    │
    ├── lambda-cloud-processor (注解处理器)
    │
    ├── lambda-cloud-starter-web (Web)
    │   ├── lambda-cloud-starter-actuator
    │   ├── lambda-cloud-starter-swagger
    │   └── lambda-cloud-starter-websocket
    │
    ├── lambda-cloud-starter-feign (远程调用)
    │   └── lambda-cloud-starter-webclient
    │
    ├── lambda-cloud-starter-security (安全)
    │   ├── lambda-cloud-starter-sms
    │   └── lambda-cloud-starter-redis
    │
    ├── lambda-cloud-starter-datasource (数据源)
    │   ├── lambda-cloud-starter-mybatis
    │   └── lambda-cloud-starter-liquibase
    │
    ├── lambda-cloud-starter-redis (缓存)
    │   └── lambda-cloud-starter-cache
    │
    ├── lambda-cloud-starter-gateway (网关)
    │
    ├── lambda-cloud-starter-netty (网络通信)
    │   └── lambda-cloud-starter-ykc
    │
    ├── lambda-cloud-starter-kafka (消息队列)
    ├── lambda-cloud-starter-rocketmq
    │
    ├── lambda-cloud-starter-nacos (服务治理)
    │
    ├── lambda-cloud-starter-dubbo (RPC)
    │
    ├── lambda-cloud-starter-oss (对象存储)
    │
    ├── lambda-cloud-starter-sse (服务端推送)
    │
    └── lambda-cloud-starter-logger (日志)
         └── lambda-cloud-starter-test
```
