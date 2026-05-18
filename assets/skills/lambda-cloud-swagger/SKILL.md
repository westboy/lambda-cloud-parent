---
name: "lambda-cloud-swagger"
description: "Lambda Cloud API文档配置指南。当用户需要配置SpringDoc OpenAPI文档、Bearer Token安全声明或Swagger UI时调用。"
---

# Lambda Cloud API 文档

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-swagger</artifactId>
</dependency>
```

## 配置

配置前缀: `lambda.api-docs`

```yaml
spring:
  application:
    name: user-service

lambda:
  api-docs:
    title: 用户服务 API
    enabled: true
    version: 2026.1.1
    open-api-version: OPENAPI_3_1
    token-enabled: true
    token-name: Authorization
    token-scheme-name: bearerAuth
    token-bearer-format: JWT
    token-scheme: bearer
    token-description: "在此输入 Bearer Token"
    doc-uri: /swagger-ui.html
```

### 配置项说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `title` | API 标题 | 空 |
| `enabled` | 是否启用 | `false` |
| `version` | API 版本 | `1.0.0` |
| `open-api-version` | OpenAPI 版本 | `OPENAPI_3_1` |
| `token-enabled` | 是否注入 Bearer Token | `true` |
| `token-name` | Token 请求头名 | `Authorization` |
| `token-scheme-name` | 安全方案名 | `bearerAuth` |
| `token-bearer-format` | Bearer 格式 | `JWT` |
| `token-scheme` | 认证方案 | `bearer` |
| `token-description` | Token 输入提示 | `在此输入 Bearer Token` |
| `doc-uri` | 文档入口路径 | `/swagger-ui.html` |

## 默认分组规则

`GroupedOpenApi` 默认配置 (`@ConditionalOnMissingBean`，可被用户自定义覆盖):
- 分组名: `spring.application.name` (缺省回退 `unknown`)
- 路径匹配: `/**`
- 仅包含标注 `@Operation` 的方法

## 控制器注解

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户 CRUD 操作")
public class UserController {

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据 ID 获取用户信息")
    public UserVO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public UserVO createUser(@RequestBody UserCreateDTO dto) {
        return userService.createUser(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public UserVO updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据 ID 删除用户")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
```

## DTO 注解

```java
@Data
@Schema(description = "用户创建请求")
public class UserCreateDTO {

    @Schema(description = "用户名", example = "zhangsan", required = true)
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;
}
```

## 分组文档

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("用户模块")
            .pathsToMatch("/api/users/**")
            .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
            .group("订单模块")
            .pathsToMatch("/api/orders/**")
            .build();
    }
}
```

## Swagger 文档模型

框架提供两个通用文档模型类:

### Result

通用响应结果模型:

```java
@Schema(description = "通用响应结果")
public class Result {
    @Schema(description = "结果状态")
    private boolean status;
    @Schema(description = "提示信息")
    private String message;
}
```

### Page

分页信息模型:

```java
@Schema(description = "分页信息")
public class Page<T> {
    @Schema(description = "当前页码")
    private Integer number;
    @Schema(description = "每页的数据量")
    private Integer size;
    @Schema(description = "总记录数")
    private Long total;
    @Schema(description = "总页数")
    private Integer pages;
    @Schema(description = "数据列表")
    private List<T> data;
}
```

## 文档禁用

当 `lambda.api-docs.enabled=false` 或未配置时:
- `SwaggerDisabledFilter` (record 类型) 生效，接受 `docUri` 构造参数
- 访问 `doc-uri` 返回 404

生产环境建议禁用:
```yaml
lambda:
  api-docs:
    enabled: false
```

## Gateway 文档聚合

在网关中配置下游服务文档路由:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          metadata:
            api-docs: /v3/api-docs
            api-name: 用户服务
            api-order: 1
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          metadata:
            api-docs: /v3/api-docs
            api-name: 订单服务
            api-order: 2
```

聚合后通过网关访问 `/v3/api-docs/swagger-config` 获取所有服务文档列表。

## Knife4j 增强 (可选)

如需使用 Knife4j UI, 可额外引入:

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
</dependency>
```

访问 `/doc.html` 使用 Knife4j 增强 UI。

## 最佳实践

1. **注解规范**: 所有控制器方法都应标注 `@Operation`
2. **分组管理**: 按模块创建分组, 便于文档导航
3. **Token 配置**: 开发环境启用 `token-enabled`, 方便调试认证接口
4. **生产安全**: 生产环境禁用文档 (`enabled: false`)
5. **版本管理**: `version` 字段与项目版本保持一致
6. **描述完善**: 为 DTO 字段添加 `@Schema` 注解, 提高文档可读性
