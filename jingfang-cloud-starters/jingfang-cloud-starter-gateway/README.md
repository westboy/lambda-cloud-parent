### jingfang-cloud-starter-gateway 项目介绍及使用说明

#### 简介
`jingfang-cloud-starter-gateway` 是一个基于Spring Cloud Gateway的网关服务模块，主要用于在微服务架构中作为API网关，提供统一的请求入口。它包含了网关的自动配置、安全过滤、跨域支持、WebSocket支持等功能，以简化开发者在项目中对网关服务的管理。

#### 主要功能
1. **网关自动配置**：模块中包含了Spring Cloud Gateway的自动配置功能，可以自动读取配置文件中的网关信息并进行配置。
2. **安全过滤**：支持通过Sa-Token进行安全过滤，实现权限控制和身份验证。
3. **跨域支持**：支持跨域请求配置，方便前端应用进行跨域调用。
4. **WebSocket支持**：支持WebSocket连接，可以用于实时通信场景。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `spring-cloud-starter-gateway`：提供Spring Cloud Gateway的基本功能。
- `spring-cloud-starter-loadbalancer`：集成loadbalancer实现客户端负载均衡。

#### 使用方式
要在你的项目中使用 `jingfang-cloud-starter-gateway`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>jingfang-cloud-starter-gateway</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置网关信息：
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: your-service
          uri: lb://your-service
          predicates:
            - Path=/your-service/**
```

通过上述配置和依赖添加，你可以在项目中使用`jingfang-cloud-starter-gateway`提供的网关服务功能，简化网关服务的配置和管理。