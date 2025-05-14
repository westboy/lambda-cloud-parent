### lambda-cloud-starter-mybatis 项目介绍及使用说明

#### 简介
`lambda-cloud-starter-mybatis` 是一个基于MyBatis的持久层管理模块，主要用于在微服务架构中实现数据库访问。它包含了MyBatis的自动配置、数据源配置、SQL映射文件管理等功能，以简化开发者在项目中对持久层的管理。

#### 主要功能
1. **MyBatis自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的MyBatis信息并进行配置。
2. **数据源配置**：支持配置多个数据源，适用于需要连接多个数据库的场景。
3. **SQL映射文件管理**：支持SQL映射文件的管理，可以将SQL语句与Java代码分离，提高可维护性。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `mybatis-spring-boot-starter`：提供MyBatis的基本功能。
- `dynamic-datasource-spring-boot3-starter`：提供动态数据源切换的功能。
- `mybatis-plus`：提供增强的MyBatis功能，如代码生成等。

#### 使用方式
要在你的项目中使用 `lambda-cloud-starter-mybatis`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>lambda-cloud-starter-mybatis</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置MyBatis信息：
```yaml
mybatis:
  configuration:
    map-underscore-to-camel-case: true
  mapper-locations: classpath:mapper/*.xml
```

通过上述配置和依赖添加，你可以在项目中使用`lambda-cloud-starter-mybatis`提供的持久层管理功能，简化数据库访问的配置和使用。
