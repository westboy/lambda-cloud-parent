<p align="center">
	<img alt="logo" src="assets/logo.png" width="512" height="320">
</p>
<p align="center">
  <a href="https://gitee.com/lamuda-cloud/lamuda-cloud-parent">
    <img src="https://img.shields.io/badge/lambda--cloud-2025.1.1--SNAPSHOT-brightgreen" alt="Lambda Fusion">
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

**Lambda Fusion** 是一个基于 **Spring Cloud 2025.0.0** 和 **Spring Boot 3.5.3** 构建的企业级微服务开发框架，构建的企业级微服务开发框架。它旨在帮助中小型企业降低微服务架构的开发与运维成本。框架对多种主流中间件进行了统一封装与自动化配置，具备 开箱即用、高度模块化、易于扩展 等特性，是快速搭建稳定、可维护的企业级分布式系统的理想选择。

## 核心优势

- 🚀 **现代化技术栈**：基于 Spring Boot 3.5.3、Spring Cloud 2025.0.0、Spring Cloud Alibaba 2025.0.0.0
- 🧩 **高度模块化**：25+ 个独立模块，支持灵活组合，按需引入，避免依赖膨胀
- 🔐 **企业级安全**：集成 Sa-Token，支持表单登录、短信登录、HMAC 签名认证、第三方登录等多种认证方式
- 💾 **MyBatis Plus 增强**：扩展 Mapper、自动填充、多租户、字段加密、数据权限等企业级功能开箱即用
- 🔧 **自动化配置**：所有中间件均提供自动配置，零配置即可快速启动，支持细粒度定制
- 🌐 **微服务全栈支持**：Dubbo、OpenFeign 双 RPC、Nacos 注册中心、Gateway 网关、Kafka/RocketMQ 消息队列
- 📦 **统一依赖管理**：通过 `lambda-cloud-starter-dependencies` 管理所有组件版本，避免依赖冲突

## 技术栈

| 分类 | 技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| **核心框架** | JDK | 21 | 运行环境，支持虚拟线程 |
| | Spring Boot | 3.5.3 | 应用框架 |
| | Spring Cloud | 2025.0.0 | 微服务框架 |
| | Spring Cloud Alibaba | 2025.0.0.0 | 阿里巴巴微服务解决方案 |
| | Spring AI | 1.0.0 | AI 应用开发支持 |
| **数据持久化** | MyBatis Plus | 3.5.12 | ORM 框架，支持扩展 Mapper、多租户、字段加密 |
| | Dynamic Datasource | 4.3.1 | 动态数据源管理 |
| | MySQL | 8.2.0 | 关系型数据库 |
| | Liquibase | 4.29.1 | 数据库版本管理 |
| | Apache IoTDB | 2.0.3 | 时序数据库 |
| | P6Spy | 3.9.1 | SQL 性能监控 |
| **缓存** | Redis | - | 分布式缓存 |
| | Redisson | 3.34.1 | Redis 客户端，支持分布式锁 |
| | Caffeine | 2.9.3 | 本地缓存 |
| **消息队列** | Kafka | - | 高吞吐量消息中间件 |
| | RocketMQ | 2.3.4 | 分布式消息中间件 |
| **RPC 框架** | Dubbo | 3.3.5 | 高性能 RPC 框架 |
| | OpenFeign | - | 声明式 HTTP 客户端 |
| **安全认证** | Sa-Token | 1.43.0 | 权限认证框架，支持多种登录方式 |
| | BouncyCastle | 1.77 | 加密算法库 |
| **网络通信** | Netty | - | 高性能网络框架，支持协议引擎 |
| | OkHttp | 4.12.0 | HTTP 客户端 |
| **对象映射** | MapStruct | 1.6.0.RC1 | Java Bean 映射工具，编译期代码生成 |
| **API 文档** | Knife4j | 4.5.0 | Swagger 增强工具 |
| | SpringDoc | 2.8.9 | OpenAPI 3 文档生成 |
| **微信开发** | WxJava | 4.7.0 | 微信开发 Java SDK |
| **工具库** | Lombok | 1.18.34 | 简化 Java 代码 |
| | Guava | 33.2.1-jre | Google 核心工具库 |
| | Hutool | 5.8.41 | Java 工具集合 |
| **日志** | SLF4J | 2.0.16 | 日志门面接口 |
| **监控** | Actuator | - | 应用监控和管理 |
| | Jacoco | 0.8.12 | 代码覆盖率 |
| **代码质量** | Spotless | 2.44.4 | 代码格式化 |
| | Spotbugs | 4.9.3.0 | 代码静态分析 |
| **云原生** | Nacos | 2.3.2 | 服务注册与配置中心 |

## 项目结构概览

```
lambda-cloud-parent
├── lambda-cloud-core                    # 核心功能与通用工具包（BaseDO/DTO/VO、对象转换、工具类、异常处理）
├── lambda-cloud-processor               # 注解处理器（MapStruct 等编译时代码生成）
├── lambda-cloud-starter-dependencies    # 项目统一依赖版本管理（BOM）
├── lambda-cloud-starter-web             # 通用 Web 开发组件封装（全局异常、Jackson 定制、统一响应）
├── lambda-cloud-starter-swagger         # API 文档生成（Knife4j/SpringDoc）
├── lambda-cloud-starter-gateway         # Spring Cloud Gateway 网关模块
├── lambda-cloud-starter-mybatis         # MyBatis Plus 集成（扩展 Mapper、多租户、字段加密、数据权限）
├── lambda-cloud-starter-datasource      # 多数据源与数据库连接池管理
├── lambda-cloud-starter-liquibase       # 数据库版本管理工具集成
├── lambda-cloud-starter-iotdb           # IoTDB 时序数据库集成
├── lambda-cloud-starter-redis           # Redis 缓存与分布式锁支持（Redisson、Caffeine）
├── lambda-cloud-starter-kafka           # Kafka 消息队列集成
├── lambda-cloud-starter-rocketmq        # RocketMQ 消息队列集成
├── lambda-cloud-starter-dubbo           # Dubbo RPC 集成支持
├── lambda-cloud-starter-feign           # 声明式 HTTP 客户端支持（OpenFeign）
├── lambda-cloud-starter-security        # 安全认证模块（表单登录、短信登录、HMAC、第三方登录）
├── lambda-cloud-starter-netty           # Netty 网络通信框架（协议引擎、TCP 长连接、性能监控）
├── lambda-cloud-starter-websocket       # WebSocket 通信支持
├── lambda-cloud-starter-sse             # Server-Sent Events 支持
├── lambda-cloud-starter-actuator        # 健康检查与监控集成
├── lambda-cloud-starter-logger          # 日志统一采集与追踪
├── lambda-cloud-starter-oss             # 对象存储（MinIO、OSS）支持
├── lambda-cloud-starter-sms             # 短信网关支持
├── lambda-cloud-starter-wxjava          # 微信开发 SDK 集成
├── lambda-cloud-starter-ykc             # 云快充协议支持（基于 Netty 协议引擎）
├── lambda-cloud-starter-webclient       # WebClient HTTP 客户端支持
├── lambda-cloud-starter-nacos           # Nacos 服务注册与配置中心集成
└── lambda-cloud-starter-test            # 测试工具与集成测试支持
```

## 快速开始

### 环境要求

- **JDK**：21 或更高版本
- **构建工具**：Maven 3.6+

### 添加依赖

在您的 Maven 项目中引入 Lambda Cloud 的 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.lambda.cloud</groupId>
            <artifactId>lambda-cloud-starter-dependencies</artifactId>
            <version>2025.1.1-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

然后根据需要添加相应的 starter 依赖，例如：

```xml
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
    
    <!-- MyBatis Plus 模块 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-mybatis</artifactId>
    </dependency>
    
    <!-- Redis 缓存模块 -->
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-redis</artifactId>
    </dependency>
</dependencies>
```

## 应用场景

- 🏢 **企业级微服务与 SaaS 平台**  
  适用于快速构建企业信息化系统与 SaaS 应用。框架内置多租户管理、数据权限、安全认证等核心能力，帮助企业在微服务架构下实现高效、稳定的业务系统。

- 📱 **IoT 设备接入与管理**  
  基于 Netty 协议引擎，支持高并发 TCP 长连接与协议自动解析。可广泛应用于充电桩、网关、传感器等物联网设备的接入、监控与远程控制。

- 📊 **时序数据与大数据分析平台**  
  集成 IoTDB 时序数据库与多源数据处理能力，支持高性能采集、实时查询与可视化分析，适用于工业监控、能耗管理等数据密集型场景。

- 🛍️ **电商与交易系统**  
  结合 Redis 缓存、Kafka 消息队列与分布式锁机制，轻松应对高并发下的库存、订单、支付等核心业务，确保系统高可用与一致性。

- 💳 **支付与清结算平台**  
  提供 HMAC 签名认证、多数据源管理与分布式事务支持，助力构建安全可靠的支付网关与清结算系统。

- 🌐 **开放平台与网关服务**  
  集成 Dubbo、OpenFeign、Gateway 与 Knife4j 文档体系，帮助企业快速搭建统一的 API 接入与管理平台，实现服务聚合与安全控制。

- 🛡️ **高安全互联网应用**  
  提供基于 Sa-Token 的统一认证体系，结合 XSS/CSRF 防护、字段加密与审计日志功能，为安全敏感型应用提供全方位防护。

## License

Lambda Cloud 采用 Apache License 2.0 协议，详见 [LICENSE](LICENSE) 文件。

## 交流群

QQ交流群：47736663 [点击加入](https://qm.qq.com/q/EBIJVZBVGE) 