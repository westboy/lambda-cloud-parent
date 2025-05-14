### lambda-cloud-starter-web 项目介绍及使用说明

#### 简介
`lambda-cloud-starter-web` 是一个基于Spring Boot的Web应用模块，主要用于在微服务架构中实现Web相关的功能。它包含了Web自动配置、Web相关工具、模板引擎等功能，以简化开发者在项目中对Web应用的管理。

#### 主要功能
1. **Web自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的Web信息并进行配置。
2. **Web相关工具**：支持集成各种Web相关工具，如Spring MVC、Thymeleaf等。
3. **模板引擎**：提供模板引擎功能，可以用于动态生成HTML页面。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `spring-boot-starter-web`：提供Spring Boot的Web基本功能。
- `spring-boot-starter-thymeleaf`：集成Thymeleaf模板引擎。

#### 使用方式
要在你的项目中使用 `lambda-cloud-starter-web`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>lambda-cloud-starter-web</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置Web信息：
```yaml
spring:
  mvc:
    view:
      prefix: /WEB-INF/views/
      suffix: .html
```

通过上述配置和依赖添加，你可以在项目中使用`lambda-cloud-starter-web`提供的Web应用功能，简化Web应用的配置和使用。
