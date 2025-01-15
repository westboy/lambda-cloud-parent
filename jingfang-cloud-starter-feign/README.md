### jingfang-cloud-starter-feign 项目介绍及使用说明

#### 简介
`jingfang-cloud-starter-feign` 是一个基于Feign的客户端模块，主要用于在微服务架构中进行服务间的调用。Feign是一个声明式的Web服务客户端，它使得编写Web服务客户端变得更加简单。通过`jingfang-cloud-starter-feign`，开发者可以轻松地在项目中集成Feign客户端，实现服务间的通信。

#### 主要功能
1. **声明式REST客户端**：使用Feign可以像使用Spring MVC注解一样来定义REST客户端。
2. **集成Hystrix**：支持与Hystrix集成，提供熔断和降级功能，增强系统的容错性。
3. **集成Ribbon**：支持客户端负载均衡。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `spring-cloud-starter-openfeign`：提供Feign的基本功能。
- `spring-cloud-starter-netflix-hystrix`：集成Hystrix提供熔断功能。
- `spring-cloud-starter-netflix-ribbon`：集成Ribbon实现客户端负载均衡。

#### 使用方式
要在你的项目中使用 `jingfang-cloud-starter-feign`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>jingfang-cloud-starter-feign</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置Feign客户端信息：
```yaml
feign:
  hystrix:
    enabled: true
  client:
    config:
      defaults:
        connectTimeoutMillis: 5000
        readTimeoutMillis: 5000
```

通过上述配置和依赖添加，你可以在项目中使用`jingfang-cloud-starter-feign`提供的Feign客户端功能，简化服务间的调用和通信。