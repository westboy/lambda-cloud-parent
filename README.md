# Lambda Cloud 微服务开发框架

<p>
  <a href="https://gitee.com/lamuda-cloud/lamuda-cloud-parent">
    <img src="https://img.shields.io/badge/lambda--cloud-1.0.0--SNAPSHOT-brightgreen" alt="Lambda Cloud">
  </a>
  <a href="https://spring.io/projects/spring-boot">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen" alt="Spring Boot">
  </a>
  <a href="https://spring.io/projects/spring-cloud">
    <img src="https://img.shields.io/badge/Spring%20Cloud-2025.0.0-brightgreen" alt="Spring Cloud">
  </a>
  <a href="https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html">
    <img src="https://img.shields.io/badge/JDK-21-blue" alt="JDK">
  </a>
</p>

**Lambda Cloud** 是一个基于 **Spring Cloud 2025.0.0** 和 **Spring Boot 3.5.3** 构建的企业级微服务开发框架，旨在简化中小型企业在微服务架构下的开发成本与运维复杂度。该框架对多种主流中间件进行了统一封装与自动化配置，具备开箱即用、高度模块化、易于扩展等特性，是快速搭建企业级分布式系统的理想选择。

## 核心优势

- 🚀 **现代化技术栈**：基于最新的 Spring Boot 3.5.3 和 Spring Cloud 2025.0.0 构建，始终与技术前沿同步
- 🧩 **模块化设计**：支持灵活组合各类中间件能力，适配不同项目需求，实现按需定制
- ⚙️ **自动化配置**：封装各类基础设施配置，简化开发流程，提高上线效率，减少重复工作
- 🛠️ **快速开发支持**：提供通用开发脚手架，业务团队可专注于核心业务逻辑实现，提升开发效率
- 🌐 **完整的微服务能力覆盖**：支持服务治理、安全认证、链路追踪等能力，构建健壮的微服务架构
- 📦 **统一依赖管理**：通过 `lambda-cloud-starter-dependencies` 管理所有组件版本，避免依赖冲突

## 技术栈

| 分类 | 技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| 核心框架 | JDK | 21 | 运行环境 |
| | Spring Boot | 3.5.3 | 核心框架 |
| | Spring Cloud | 2025.0.0 | 微服务框架 |
| | Spring Cloud Alibaba | 2023.0.3.3 | 阿里巴巴微服务解决方案 |
| 数据库 | MyBatis Plus | 3.5.12 | ORM 框架 |
| | MySQL | 8.2.0 | 关系型数据库 |
| | Liquibase | 4.29.1 | 数据库迁移工具 |
| | Apache IoTDB | 2.0.3 | 时序数据库 |
| 缓存 | Redis | - | 分布式缓存 |
| | Redisson | 3.34.1 | Redis 客户端 |
| 消息队列 | Kafka | - | 高吞吐量消息中间件 |
| | RocketMQ | 2.3.4 | 分布式消息中间件 |
| RPC框架 | Dubbo | 3.3.1 | 高性能RPC框架 |
| 安全框架 | Sa-Token | 1.43.0 | 权限认证框架 |
| API文档 | Knife4j | 4.5.0 | Swagger增强工具 |
| | SpringDoc | 2.8.9 | OpenAPI 3 文档生成 |
| 微信开发 | WxJava | 4.7.0 | 微信开发Java SDK |
| 工具类 | Lombok | 1.18.34 | 简化Java代码工具 |
| | Guava | 33.2.1-jre | Google核心Java库 |
| | MapStruct | 1.6.0.RC1 | Java Bean映射工具 |
| | Hutool | 5.8.29 | Java工具库 |
| 通信 | Netty | - | 高性能网络通信框架 |
| | OkHttp | 4.12.0 | HTTP客户端 |
| 日志 | SLF4J | 2.0.16 | 日志门面接口 |
| 云原生 | Nacos | 2.3.2 | 服务注册与配置中心 |

## 项目结构概览

```
lambda-cloud-parent
├── lambda-cloud-core                    # 核心功能与通用工具包
├── lambda-cloud-starter-actuator        # 健康检查与监控集成
├── lambda-cloud-starter-datasource      # 多数据源与数据库连接池管理
├── lambda-cloud-starter-dependencies    # 项目统一依赖版本管理
├── lambda-cloud-starter-dubbo           # Dubbo RPC 集成支持
├── lambda-cloud-starter-feign           # 声明式 HTTP 客户端支持
├── lambda-cloud-starter-gateway         # 基于 Spring Cloud Gateway 的网关模块
├── lambda-cloud-starter-iotdb           # IoTDB 时序数据库集成
├── lambda-cloud-starter-kafka           # Kafka 消息队列集成
├── lambda-cloud-starter-liquibase       # 数据库变更管理工具集成
├── lambda-cloud-starter-logger          # 日志统一采集与追踪
├── lambda-cloud-starter-mybatis         # MyBatis ORM 框架集成
├── lambda-cloud-starter-netty           # Netty 网络通信框架集成
├── lambda-cloud-starter-oss             # 对象存储（如 MinIO、OSS）支持
├── lambda-cloud-starter-redis           # Redis 缓存与分布式锁支持
├── lambda-cloud-starter-rocketmq        # RocketMQ 消息队列集成
├── lambda-cloud-starter-security        # 基于 Sa-Token 的安全认证模块
├── lambda-cloud-starter-sms             # 短信网关支持
├── lambda-cloud-starter-sse             # Server-Sent Events 支持
├── lambda-cloud-starter-swagger         # API 文档生成（Swagger/OpenAPI）
├── lambda-cloud-starter-test            # 测试工具与集成测试支持
├── lambda-cloud-starter-web             # 通用 Web 开发组件封装
└── lambda-cloud-starter-websocket       # WebSocket 通信支持
```

## 快速开始

### 环境要求

- JDK 21 或更高版本
- Maven 3.6+ 或 Gradle 7+
- Redis 5.0+（如使用缓存相关功能）
- MySQL 8.0+（如使用数据库相关功能）

### 添加依赖

在您的 Maven 项目中引入 Lambda Cloud 的 BOM：

```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.lambda.cloud</groupId>
            <artifactId>lambda-cloud-starter-dependencies</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

然后根据需要添加相应的 starter 依赖，例如：

```
<dependencies>
    <!-- Web 模块 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-web</artifactId>
    </dependency>
    
    <!-- 安全认证模块 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-security</artifactId>
    </dependency>
    
    <!-- Redis 缓存模块 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-redis</artifactId>
    </dependency>
</dependencies>
```

## 应用场景

- 快速构建企业级微服务项目
- 多模块协作的分布式系统开发
- 微服务架构的技术落地与标准化支撑
- 面向中小型团队的低门槛高效率开发平台

## License

Lambda Cloud 采用 Apache License 2.0 协议，详见 [LICENSE](LICENSE) 文件。