---
name: "lambda-cloud-logging"
description: "Lambda Cloud 日志与操作审计指南。当用户需要配置操作日志采集(@OperationLog)、自定义日志存储或使用LogContext时调用。"
---

# Lambda Cloud 日志管理

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-logger</artifactId>
</dependency>
```

## @OperationLog 注解

在方法上标注, 自动采集操作日志:

```java
@OperationLog(value = "创建用户", module = "用户管理", type = "CREATE")
public UserVO createUser(@RequestBody UserCreateDTO dto) {
    return userService.createUser(dto);
}

@OperationLog(value = "更新用户", module = "用户管理", type = "UPDATE")
public UserVO updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
    return userService.updateUser(id, dto);
}

@OperationLog(value = "删除用户", module = "用户管理", type = "DELETE")
public void deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
}
```

### 注解字段

| 字段 | 说明 | 默认值 |
|------|------|--------|
| `value` | 操作标识 | 空 (回退到 `类名.方法名`) |
| `module` | 模块名 | `"模块"` |
| `type` | 操作类型 | 空 (回退到 HTTP Method) |

## 采集链路

`OperationLoggerAdvice` (AOP) 处理流程:

1. 拦截标注 `@OperationLog` 的方法
2. 解析目标方法与参数注解, 启动 StopWatch
3. 获取当前 HttpServletRequest 与当前用户 (OperatorUtils)
4. 组装 OperationLogRecord (方法/模块/类型/操作者/IP)
5. 组装 OperationContext (URI/Query 参数/RequestBody)
6. 执行业务方法
   - 成功: 记录返回值
   - 失败: 记录异常堆栈后继续抛出
7. finally 中补齐时间、耗时并调用 `operationService.save()`
8. 清理 LogContext (MDC.clear())

## 日志模型

### OperationLogRecord

| 字段 | 说明 |
|------|------|
| `method` | 方法全名 |
| `module` | 模块名 |
| `description` | 操作描述 |
| `httpMethod` | HTTP 方法 |
| `time` | 操作时间 |
| `duration` | 耗时 (毫秒) |
| `detail` | 详细信息 (JSON) |
| `operator` | 操作人名称 |
| `operatorId` | 操作人 ID |
| `tenantId` | 租户 ID |
| `ipAddress` | 客户端 IP |

### OperationContext

| 字段 | 说明 |
|------|------|
| `operationId` | 操作唯一 ID |
| `uri` | 请求 URI |
| `parameters` | Query 参数 |
| `body` | RequestBody 参数 |
| `result` | 返回值或异常信息 |

## LogContext 上下文

基于 SLF4J MDC, 提供以下上下文管理:

| 方法 | 说明 |
|------|------|
| `setDetail(String)` | 设置日志详情 (覆盖默认 OperationContext JSON) |
| `getDetail()` | 获取当前线程的日志详情 |
| `setDescription(String)` | 设置日志描述 (覆盖 Swagger @Operation 描述) |
| `getDescription()` | 获取当前线程的日志描述 |
| `clear()` | 清除当前线程的所有 MDC 上下文 |

```java
@OperationLog(value = "复杂操作", module = "业务模块")
public void complexOperation() {
    // 设置详细信息 (覆盖默认 OperationContext JSON)
    LogContext.setDetail("{\"customKey\": \"customValue\"}");

    // 设置描述 (覆盖 Swagger @Operation 描述)
    LogContext.setDescription("自定义操作描述");

    // 业务逻辑...
}
```

## 自定义日志存储

默认 `DefaultOperationServiceImpl` 仅输出到应用日志。如需持久化, 实现 `OperationService`:

```java
@Component
public class DbOperationService implements OperationService {

    @Autowired
    private OperationLogRepository logRepository;

    @Override
    public void save(OperationLogRecord logRecord) {
        // 保存到数据库
        OperationLogEntity entity = new OperationLogEntity();
        entity.setMethod(logRecord.getMethod());
        entity.setModule(logRecord.getModule());
        entity.setDescription(logRecord.getDescription());
        entity.setHttpMethod(logRecord.getHttpMethod());
        entity.setTime(logRecord.getTime());
        entity.setDuration(logRecord.getDuration());
        entity.setDetail(logRecord.getDetail());
        entity.setOperator(logRecord.getOperator());
        entity.setOperatorId(logRecord.getOperatorId());
        entity.setTenantId(logRecord.getTenantId());
        entity.setIpAddress(logRecord.getIpAddress());
        logRepository.insert(entity);
    }
}
```

### Kafka 输出 (可选)

配置前缀: `lambda.logging`:

```yaml
lambda:
  logging:
    operation:
      kafka:
        enabled: true
        topic: operation-logs
```

**注意**: Kafka 发送需自定义 `OperationService` 实现。

## 配置模型

```yaml
lambda:
  logging:
    operation:
      kafka:
        enabled: false
        topic: operation-logs
```

## 最佳实践

1. **注解使用**: 所有写操作都应标注 `@OperationLog`
2. **模块划分**: 按业务模块命名, 便于日志分析
3. **敏感参数**: 避免在 `@RequestBody` 中包含密码等敏感信息
4. **存储选择**: 开发环境用默认日志, 生产环境建议持久化到数据库
5. **异步存储**: 大量日志时考虑异步写入, 避免影响主业务性能
6. **日志清理**: 定期清理过期日志, 避免表过大
