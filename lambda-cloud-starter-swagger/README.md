# Lambda Cloud Swagger Starter

基于Spring Boot的API文档模块，集成SpringDoc OpenAPI。

## 功能特性

- 自动生成OpenAPI文档
- 默认分组配置
- 分页参数自动转换
- 支持禁用Swagger文档

## 配置项

```yaml
lambda:
  api-docs:
    title: API文档 # 文档标题
    enabled: true # 是否启用Swagger文档
    docUri: /swagger-ui.html # 文档访问路径
    version: 1.0.0 # API版本号
```

## 使用示例

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-swagger</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 基本配置

```yaml
lambda:
  api-docs:
    title: 用户服务API
    enabled: true
```

### 3. 接口文档注解

```java
@Operation(summary = "获取用户信息")
@GetMapping("/users/{id}")
public Result<User> getUser(@PathVariable Long id) {
    // 业务逻辑
}
```

### 4. 分页参数

```java
@Operation(summary = "分页查询用户")
@GetMapping("/users")
public Result<Page<User>> listUsers(Page page) {
    // 业务逻辑
}
```

## 注意事项

1. 默认启用Swagger文档
2. 生产环境建议禁用Swagger文档
3. 分页参数会自动转换为Page对象
4. 文档访问路径默认为/swagger-ui.html
