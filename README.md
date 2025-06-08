# 项目简介：**lambda-cloud 微服务开发框架**

**lambda-cloud** 是一个基于 **Spring Cloud** 构建的企业级微服务开发框架，旨在简化中小型企业在微服务架构下的开发成本与运维复杂度。该框架对多种主流中间件进行了统一封装与自动化配置，具备开箱即用、高度模块化、易于扩展等特性，是快速搭建企业级分布式系统的理想选择。

框架采用模块化设计，核心功能按业务特性拆分为多个可插拔的子模块，覆盖服务注册与发现、配置管理、API 网关、权限认证、消息中间件、ORM 框架、任务调度、日志跟踪、安全防护等多个方面。开发人员可以根据业务需求按需引入子模块，从而大幅降低技术选型与框架搭建的成本。

### 核心优势：

- ✅ **标准化架构**：遵循 Spring Cloud 生态标准，具备良好的系统兼容性与社区支持。
- ✅ **模块化设计**：支持灵活组合各类中间件能力，适配不同项目需求。
- ✅ **自动化配置**：封装各类基础设施配置，简化开发流程，提高上线效率。
- ✅ **快速开发支持**：提供通用开发脚手架，业务团队可专注于核心业务逻辑实现。
- ✅ **完整的微服务能力覆盖**：支持服务治理、分布式事务、安全认证、链路追踪等能力。

### 项目结构概览：

```
lambda-cloud-parent
    ├── lambda-cloud-core # 核心功能与通用工具包
    ├── lambda-cloud-starter-actuator # 健康检查与监控集成
    ├── lambda-cloud-starter-datasource # 多数据源与数据库连接池管理
    ├── lambda-cloud-starter-dependencies # 项目统一依赖版本管理
    ├── lambda-cloud-starter-dubbo # Dubbo RPC 集成支持
    ├── lambda-cloud-starter-feign # 声明式 HTTP 客户端支持
    ├── lambda-cloud-starter-gateway # 基于 Spring Cloud Gateway 的网关模块
    ├── lambda-cloud-starter-kafka # Kafka 消息队列集成
    ├── lambda-cloud-starter-liquibase # 数据库变更管理工具集成
    ├── lambda-cloud-starter-logger # 日志统一采集与追踪
    ├── lambda-cloud-starter-lucene # Lucene 全文检索支持
    ├── lambda-cloud-starter-mybatis # MyBatis ORM 框架集成
    ├── lambda-cloud-starter-oss # 对象存储（如 MinIO、OSS）支持
    ├── lambda-cloud-starter-plugin # 插件式开发支持
    ├── lambda-cloud-starter-redis # Redis 缓存与分布式锁支持
    ├── lambda-cloud-starter-security # 基于 Sa-Token 的安全认证模块（非 Spring Security）
    ├── lambda-cloud-starter-sms # 短信网关支持
    ├── lambda-cloud-starter-swagger # API 文档生成（Swagger/OpenAPI）
    ├── lambda-cloud-starter-test # 测试工具与集成测试支持
    ├── lambda-cloud-starter-web # 通用 Web 开发组件封装
    └── lambda-cloud-starter-websocket # WebSocket 通信支持
```

### 应用场景：

- 快速构建企业级微服务项目
- 多模块协作的分布式系统开发
- 微服务架构的技术落地与标准化支撑
- 面向中小型团队的低门槛高效率开发平台