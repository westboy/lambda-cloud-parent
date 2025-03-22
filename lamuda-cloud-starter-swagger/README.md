### lamuda-cloud-starter-swagger 项目介绍及使用说明

#### 简介
`lamuda-cloud-starter-swagger` 是一个基于Swagger的API文档管理模块，主要用于在微服务架构中自动生成和展示API文档。它包含了Swagger的自动配置、API文档生成、API文档展示等功能，以简化开发者在项目中对API文档的管理。

#### 主要功能
1. **Swagger自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的Swagger信息并进行配置。
2. **API文档生成**：支持自动生成API文档，可以根据Spring MVC的注解自动生成API文档。
3. **API文档展示**：提供API文档展示功能，可以通过Swagger UI展示生成的API文档。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `springfox-boot-starter`：提供Swagger的基本功能。
- `springfox-swagger-ui`：提供Swagger UI展示功能。

#### 使用方式
要在你的项目中使用 `lamuda-cloud-starter-swagger`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>lamuda-cloud-starter-swagger</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置Swagger信息：
```yaml
springfox:
  documentation:
    openapi:
      v3:
        enabled: true
```

通过上述配置和依赖添加，你可以在项目中使用`lamuda-cloud-starter-swagger`提供的API文档管理功能，简化API文档的配置和使用。