### jingfang-cloud-starter-websocket 项目介绍及使用说明

#### 简介
`jingfang-cloud-starter-websocket` 是一个基于WebSocket的实时通信模块，主要用于在微服务架构中实现实时通信功能。它包含了WebSocket的自动配置、消息发送、消息接收等功能，以简化开发者在项目中对WebSocket的管理。

#### 主要功能
1. **WebSocket自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的WebSocket信息并进行配置。
2. **消息发送**：支持发送消息到指定的WebSocket客户端或所有客户端。
3. **消息接收**：提供消息接收功能，可以接收来自WebSocket客户端的消息。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `spring-boot-starter-websocket`：提供WebSocket的基本功能。
- `spring-boot-starter-messaging`：提供消息发送和接收功能。

#### 使用方式
要在你的项目中使用 `jingfang-cloud-starter-websocket`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>jingfang-cloud-starter-websocket</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置WebSocket信息：
```yaml
spring:
  websocket:
    handshake:
      supportedProtocols: [websocket, sctp]
```

通过上述配置和依赖添加，你可以在项目中使用`jingfang-cloud-starter-websocket`提供的实时通信功能，简化WebSocket的配置和使用。