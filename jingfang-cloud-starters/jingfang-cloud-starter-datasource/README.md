### jingfang-cloud-starter-datasource 项目介绍及使用说明

#### 简介
`jingfang-cloud-starter-datasource` 是一个数据源管理模块，主要负责配置和管理项目中的数据源。它包含了数据源的自动配置、动态数据源切换等功能，以简化开发者在项目中对数据源的管理。

#### 主要功能
1. **数据源自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的数据源信息并进行配置。
2. **动态数据源切换**：支持在运行时动态切换数据源，适用于需要连接多个数据库的场景。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `spring-boot-starter-jdbc`：用于配置和管理JDBC数据源。
- `dynamic-datasource-spring-boot3-starter`：提供动态数据源切换的功能。
- `liquibase-core`：用于数据库的迁移管理。

#### 使用方式
要在你的项目中使用 `jingfang-cloud-starter-datasource`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>jingfang-cloud-starter-datasource</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置数据源信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
```

通过上述配置和依赖添加，你可以在项目中使用`jingfang-cloud-starter-datasource`提供的数据源管理功能，简化数据源的配置和管理。