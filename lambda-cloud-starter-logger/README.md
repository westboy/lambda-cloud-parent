# lambda-cloud-starter-logger

`lambda-cloud-starter-logger` 提供基于 AOP 的操作日志采集能力，通过 `@OperationLog` 注解对方法调用进行结构化记录，并将日志持久化委托给可替换的 `OperationService`。

## 模块定位

- 为业务方法提供统一的“操作日志”采集切面。
- 自动捕获请求、参数、返回值、异常、耗时等上下文。
- 默认输出到应用日志，支持业务侧替换存储实现。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ LoggerAutoConfiguration.java
└─ LoggingProperties.java

src/main/java/com/lambda/cloud/logger/
├─ annotation/OperationLog.java
├─ advices/
│  ├─ AbstractAdvice.java
│  └─ OperationLoggerAdvice.java
├─ context/LogContext.java
├─ model/
│  ├─ OperationLogRecord.java
│  └─ OperationContext.java
└─ service/
   ├─ OperationService.java
   └─ impl/DefaultOperationServiceImpl.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.LoggerAutoConfiguration
```

## 自动装配机制

`LoggerAutoConfiguration` 主要行为：

- 启用配置绑定：`LoggingProperties`（前缀 `lambda.logging`）
- 仅在 Servlet Web 环境注册 `OperationLoggerAdvice`
- 默认注册 `OperationService`（`DefaultOperationServiceImpl`）
  - 条件：`@ConditionalOnMissingBean`
  - 可被业务自定义实现覆盖

## 注解模型

`@OperationLog`（方法级）字段：

- `value`：操作标识，默认空（最终会回退到 `类名.方法名`）
- `module`：模块名，默认 `"模块"`
- `type`：操作类型，默认空（最终会回退到 HTTP Method）

## 日志采集链路

以 `OperationLoggerAdvice` 为核心，处理流程如下：

1. 拦截标注 `@OperationLog` 的方法（`@Around`）
2. 解析目标方法与参数注解，启动 `StopWatch`
3. 获取当前 `HttpServletRequest` 与当前用户（`OperatorUtils.getOperator()`）
4. 组装 `OperationLogRecord`（方法、模块、类型、操作者、IP）
5. 组装 `OperationContext`（URI、Query 参数、`@RequestBody` 参数）
6. 执行业务方法
   - 成功：记录返回值到 `OperationContext.result`
   - 失败：记录异常堆栈到 `OperationContext.result` 后继续抛出
7. 在 finally 中补齐时间、耗时并调用 `operationService.save(...)`
8. 清理 `LogContext`（底层调用 `MDC.clear()`）

## 上下文模型

### OperationLogRecord

主要字段：

- `method`
- `module`
- `description`
- `httpMethod`
- `time`
- `duration`（JSON 字段名 `cost`）
- `detail`
- `operator` / `operatorId`
- `tenantId`
- `ipAddress`

### OperationContext

主要字段：

- `operationId`
- `uri`
- `parameters`
- `body`
- `result`

## LogContext 用法

`LogContext` 基于 SLF4J MDC 提供两个上下文键：

- `detail`
- `description`

若业务在切面执行期间设置了这两个值：

- `detail` 优先覆盖默认 `OperationContext` JSON
- `description` 优先覆盖 Swagger `@Operation` 描述

## 配置模型

配置前缀：`lambda.logging`

可绑定字段：

- `operation.kafka.enabled`（默认 `false`）
- `operation.kafka.topic`

说明：

- 当前 starter 仅完成属性绑定，源码内未内置 Kafka 发送实现。
- 如需 Kafka 输出，需要业务侧自定义 `OperationService` 并读取这些配置。

## 最小使用示例

```java
@OperationLog(value = "创建用户", module = "用户管理", type = "CREATE")
public User createUser(@RequestBody UserCreateReq req) {
    return userService.create(req);
}
```

自定义日志落库示例：

```java
@Component
public class DbOperationService implements OperationService {
    @Override
    public void save(OperationLogRecord logRecord) {
        repository.insert(logRecord);
    }
}
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `com.lambda.cloud:lambda-cloud-core`
- `org.aspectj:aspectjweaver`
- `org.springframework.boot:spring-boot-starter-logging`
- `org.springframework:spring-web`
- `jakarta.servlet:jakarta.servlet-api`（optional）

## 当前实现约束

- 仅在 Servlet Web 环境生效，非 Web 场景不会触发切面记录。
- 无 HTTP 请求上下文时（如异步线程/非请求线程）直接跳过日志采集。
- `LogContext.clear()` 调用 `MDC.clear()`，会清空当前线程全部 MDC 键。
- `@RequestBody` 仅提取第一个命中参数，其他复杂体参数不会额外合并。
- `OperationLogRecord.operator` 与 `tenantId` 当前切面未赋值，默认实现主要填充 `operatorId`。
- `PATCH` 会被归并为 `PUT` 作为操作类型兜底值。
- 默认 `OperationService` 仅输出日志，不做持久化与可靠投递。
