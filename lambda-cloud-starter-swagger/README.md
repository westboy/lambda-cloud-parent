# lambda-cloud-starter-swagger

`lambda-cloud-starter-swagger` 提供基于 SpringDoc OpenAPI 的文档自动配置能力，包含 OpenAPI 元数据、默认分组策略、Bearer Token 安全声明，以及按配置关闭文档入口的过滤器。

## 模块定位

- 统一应用 OpenAPI 元信息与版本配置。
- 提供开箱即用的默认文档分组规则。
- 可选注入 Bearer Token 安全方案，便于调试受保护接口。
- 支持按开关禁用指定 Swagger UI 访问入口。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ SwaggerAutoConfiguration.java
└─ SwaggerProperties.java

src/main/java/com/lambda/cloud/swagger/
├─ filter/SwaggerDisabledFilter.java
└─ model/
   ├─ Page.java
   └─ Result.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.SwaggerAutoConfiguration
```

## 自动装配机制

`SwaggerAutoConfiguration` 主要输出：

- `OpenAPI`（缺省时创建）
- `GroupedOpenApi`（缺省时创建，分组名默认取 `spring.application.name`）
- `SwaggerDisabledFilter`（当 `lambda.api-docs.enabled=false` 时创建）

### OpenAPI 构建规则

- `openapi` 版本来自 `lambda.api-docs.open-api-version`
- `info.title` 来自 `lambda.api-docs.title`
- `info.version` 来自 `lambda.api-docs.version`

当 `lambda.api-docs.token-enabled=true` 时：

- 自动注入 `SecurityScheme.Type.HTTP`
- scheme 默认为 `bearer`
- bearerFormat 默认为 `JWT`
- 自动追加全局 `SecurityRequirement`

### 默认分组规则

`GroupedOpenApi` 默认配置：

- `group = spring.application.name`（缺省回退 `unknown`）
- `pathsToMatch = /**`
- 仅包含标注 `@Operation` 的方法

## 配置模型

配置前缀：`lambda.api-docs`

- `title`
- `enabled` 默认 `false`
- `doc-uri` 默认 `/swagger-ui.html`
- `version` 默认 `1.0.0`
- `open-api-version` 默认 `OPENAPI_3_1`
- `token-enabled` 默认 `true`
- `token-name` 默认 `Authorization`
- `token-scheme-name` 默认 `bearerAuth`
- `token-bearer-format` 默认 `JWT`
- `token-scheme` 默认 `bearer`
- `token-description` 默认 `在此输入 Bearer Token`

示例：

```yaml
spring:
  application:
    name: user-service

lambda:
  api-docs:
    title: 用户服务 API
    enabled: true
    version: 2026.1.1
    token-enabled: true
    doc-uri: /swagger-ui.html
```

## 文档禁用机制

当 `lambda.api-docs.enabled=false` 时，`SwaggerDisabledFilter` 生效：

- 若请求 URI 等于 `doc-uri`，返回 404
- 其他请求透传

说明：

- 该过滤器只拦截单一 `doc-uri` 路径，不会自动拦截 `/v3/api-docs` 等其它文档相关地址。

## 附带模型说明

模块内提供两个带 `@Schema` 注解的模型：

- `Page<T>`：分页响应结构（number/size/total/pages/data）
- `Result`：通用响应结构（status/message）

可作为接口文档示例模型复用。

## 依赖说明

关键依赖（见 `pom.xml`）：

- `org.springdoc:springdoc-openapi-starter-webmvc-api`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `com.lambda.cloud:lambda-cloud-starter-web`
- `com.lambda.cloud:lambda-cloud-core`

## 当前实现约束

- `lambda.api-docs.enabled` 默认值为 `false`，默认会启用禁用过滤器并阻断 `doc-uri`。
- 默认分组仅包含标注了 `@Operation` 的接口，未标注方法不会出现在文档中。
- `SwaggerDisabledFilter` 为 Servlet `Filter`，仅适用于 WebMVC 场景。
- 禁用逻辑只判断 URI 与 `doc-uri` 的精确相等，不处理尾斜杠或上下文路径差异。
- `title` 未提供默认值，若未配置可能导致文档标题为空。
