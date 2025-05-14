### lambda-cloud-starter-security 项目介绍及使用说明

#### 简介
`lambda-cloud-starter-security` 是一个安全控制模块，主要用于在微服务架构中实现权限控制和身份验证。它包含了安全的自动配置、权限控制、身份验证等功能，以简化开发者在项目中对安全控制的管理。

#### 主要功能
1. **安全自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的安全信息并进行配置。
2. **权限控制**：支持对用户进行权限控制，可以限制用户对特定资源的访问。
3. **身份验证**：提供身份验证功能，可以验证用户的身份信息。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `spring-boot-starter-security`：提供Spring Security的基本功能。
- `sa-token`：提供增强的安全控制功能，如权限控制等。

#### 使用方式
要在你的项目中使用 `lambda-cloud-starter-security`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>lambda-cloud-starter-security</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置安全信息：
```yaml
security:
  basic:
    enabled: true
  user:
    name: user
    password: password
```

通过上述配置和依赖添加，你可以在项目中使用`lambda-cloud-starter-security`提供的安全控制功能，简化权限控制和身份验证的配置和使用。
