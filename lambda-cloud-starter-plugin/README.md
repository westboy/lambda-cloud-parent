### lambda-cloud-starter-plugin 项目介绍及使用说明

#### 简介
`lambda-cloud-starter-plugin` 是一个插件管理模块，主要用于在微服务架构中实现插件的动态加载和管理。它包含了插件的自动配置、插件加载、插件执行等功能，以简化开发者在项目中对插件的管理。

#### 主要功能
1. **插件自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的插件信息并进行配置。
2. **插件加载**：支持动态加载插件，可以在运行时加载和卸载插件。
3. **插件执行**：提供插件执行功能，可以在指定的时间和条件下执行插件。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `spring-boot-starter`：提供Spring Boot的基本功能。
- `spring-cloud-starter-netflix-hystrix`：集成Hystrix提供熔断功能。
- `spring-cloud-starter-netflix-ribbon`：集成Ribbon实现客户端负载均衡。

#### 使用方式
要在你的项目中使用 `lambda-cloud-starter-plugin`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>lambda-cloud-starter-plugin</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置插件信息：
```yaml
plugin:
  directory: classpath:/plugins
  scan-package: com.yourcompany.plugins
```

通过上述配置和依赖添加，你可以在项目中使用`lambda-cloud-starter-plugin`提供的插件管理功能，简化插件的配置和使用。
