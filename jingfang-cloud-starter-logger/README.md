### jingfang-cloud-starter-logger 项目介绍及使用说明

#### 简介
`jingfang-cloud-starter-logger` 是一个日志管理模块，主要用于在微服务架构中统一管理日志。它包含了日志的自动配置、日志格式化、日志输出等功能，以简化开发者在项目中对日志的管理。

#### 主要功能
1. **日志自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的日志信息并进行配置。
2. **日志格式化**：支持自定义日志格式，可以将日志信息格式化为统一的格式进行输出。
3. **日志输出**：提供日志输出功能，可以将日志信息输出到控制台、文件等不同目的地。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `spring-boot-starter-log4j2`：提供Log4j2的日志功能。
- `spring-cloud-starter-netflix-hystrix`：集成Hystrix提供熔断功能。
- `spring-cloud-starter-netflix-ribbon`：集成Ribbon实现客户端负载均衡。

#### 使用方式
要在你的项目中使用 `jingfang-cloud-starter-logger`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>jingfang-cloud-starter-logger</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置日志信息：
```yaml
logging:
  level:
    root: info
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: app.log
```

通过上述配置和依赖添加，你可以在项目中使用`jingfang-cloud-starter-logger`提供的日志管理功能，简化日志的配置和管理。