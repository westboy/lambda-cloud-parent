# lambda-cloud-starter-logger 操作日志模块

## 目录
- [功能概述](#功能概述)
- [核心组件](#核心组件)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [使用示例](#使用示例)
- [扩展开发](#扩展开发)
- [依赖说明](#依赖说明)

## 功能概述

本模块提供基于 Spring AOP 的操作日志记录功能，通过注解驱动的方式自动记录业务操作日志。主要特性包括：

- **注解驱动**：通过 `@OperationLog` 注解标记需要记录日志的方法
- **自动记录**：自动捕获方法执行参数、返回结果、执行耗时和异常信息
- **上下文感知**：自动获取 HTTP 请求信息、用户信息和客户端 IP
- **灵活配置**：支持 Kafka 日志收集等多种扩展方式
- **线程安全**：基于 SLF4J MDC 实现线程本地日志上下文管理
- **异常隔离**：日志记录异常不影响业务流程正常执行

## 核心组件

### 自动配置类
- **LoggerAutoConfiguration**：操作日志自动配置类，负责注册相关 Bean
- **LoggingProperties**：配置属性类，绑定 `lambda.logging` 前缀的配置项

### 核心注解
- **@OperationLog**：操作日志注解，支持以下属性：
  - `value`：操作标识，描述具体操作内容（默认使用方法全限定名）
  - `module`：所属模块名称（默认为"模块"）
  - `type`：操作类型，如 CREATE、UPDATE 等（默认根据 HTTP 方法推断）

### 切面实现
- **OperationLoggerAdvice**：操作日志切面类，拦截 `@OperationLog` 注解方法并记录日志

### 服务接口
- **OperationService**：操作日志服务接口，定义日志保存方法
- **DefaultOperationServiceImpl**：默认实现，将日志输出到控制台（适用于开发测试环境）

### 数据模型
- **OperationLogRecord**：操作日志记录模型，包含操作者、方法、模块、耗时等核心信息
- **OperationContext**：操作上下文模型，包含请求 URI、参数、请求体和响应结果等详细信息

### 工具类
- **LogContext**：日志上下文工具类，基于 SLF4J MDC 实现线程本地变量管理

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-logger</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 启用自动配置

模块通过 Spring Boot 自动配置机制自动启用，无需额外配置。

### 3. 使用注解

在需要记录日志的方法上添加 `@OperationLog` 注解：

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @OperationLog(value = "用户登录", module = "用户管理", type = "LOGIN")
    @PostMapping("/login")
    public LoginResult login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
```

## 配置说明

### 基础配置

```yaml
lambda:
  logging:
    operation:
      kafka:
        enabled: false          # 是否启用 Kafka 日志收集
        topic: operation-logs   # Kafka 主题名称
```

### 配置属性说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `lambda.logging.operation.kafka.enabled` | boolean | false | 是否启用 Kafka 日志收集 |
| `lambda.logging.operation.kafka.topic` | String | - | Kafka 日志主题名称 |

## 使用示例

### 基础用法

```java
@Service
public class UserService {
    
    @OperationLog(value = "创建用户", module = "用户管理", type = "CREATE")
    public User createUser(@RequestBody User user) {
        // 业务逻辑
        return userRepository.save(user);
    }
    
    @OperationLog(value = "更新用户信息", module = "用户管理", type = "UPDATE")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        // 业务逻辑
        return userRepository.update(id, user);
    }
    
    @OperationLog(value = "删除用户", module = "用户管理", type = "DELETE")
    public void deleteUser(@PathVariable Long id) {
        // 业务逻辑
        userRepository.deleteById(id);
    }
}
```

### 使用默认值

```java
@OperationLog  // 使用默认配置
public List<User> getAllUsers() {
    return userRepository.findAll();
}
```

### 结合 Swagger 注解

```java
@Operation(summary = "用户查询", description = "根据条件查询用户列表")
@OperationLog(module = "用户管理", type = "QUERY")
@GetMapping("/search")
public PageResult<User> searchUsers(@RequestParam String keyword) {
    return userService.searchUsers(keyword);
}
```

### 日志上下文使用

```java
@OperationLog(value = "批量导入用户", module = "用户管理", type = "IMPORT")
public ImportResult importUsers(@RequestParam MultipartFile file) {
    try {
        // 设置详情描述
        LogContext.setDetail("导入文件：" + file.getOriginalFilename());
        
        ImportResult result = userService.importFromFile(file);
        
        // 设置操作说明
        LogContext.setDescription(String.format("成功导入 %d 条用户数据", result.getSuccessCount()));
        
        return result;
    } finally {
        // 清理上下文（框架会自动清理，手动清理是可选的）
        LogContext.clear();
    }
}
```

## 扩展开发

### 自定义日志服务

实现 `OperationService` 接口来自定义日志处理逻辑：

```java
@Component
public class DatabaseOperationService implements OperationService {
    
    @Autowired
    private OperationLogRepository operationLogRepository;
    
    @Override
    public void save(OperationLogRecord operationLogRecord) {
        // 保存到数据库
        OperationLogEntity entity = convertToEntity(operationLogRecord);
        operationLogRepository.save(entity);
    }
    
    private OperationLogEntity convertToEntity(OperationLogRecord record) {
        // 转换逻辑
        return new OperationLogEntity();
    }
}
```

### Kafka 集成示例

```java
@Component
@ConditionalOnProperty(name = "lambda.logging.operation.kafka.enabled", havingValue = "true")
public class KafkaOperationService implements OperationService {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${lambda.logging.operation.kafka.topic}")
    private String topic;
    
    @Override
    public void save(OperationLogRecord operationLogRecord) {
        // 发送到 Kafka
        kafkaTemplate.send(topic, operationLogRecord);
    }
}
```

## 依赖说明

### 核心依赖
- **lambda-cloud-core**：提供核心工具类和用户信息获取
- **spring-boot-starter-logging**：日志框架支持
- **aspectjweaver**：AOP 切面支持
- **spring-web**：Web 环境支持
- **jakarta.servlet-api**：Servlet API 支持（可选）

### 兼容性
- Spring Boot 3.x
- Java 17+
- Jakarta EE 9+

---

**注意**：本模块设计为开箱即用，默认提供控制台日志输出。生产环境建议实现自定义的 `OperationService` 来满足具体的日志存储需求。
