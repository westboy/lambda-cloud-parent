# lambda-cloud-starter-logger 操作日志模块

## 功能概述
本模块提供基于注解的操作日志记录功能，主要特性包括：
1. 通过@OperationLog注解标记需要记录日志的方法
2. 自动记录方法执行参数、结果和耗时
3. 支持Kafka日志收集(可选)

## 核心组件
### 自动配置类
- LoggerAutoConfiguration: 自动配置操作日志相关Bean
- LoggingProperties: 提供日志相关配置属性

### 核心注解
- @OperationLog: 标记需要记录操作日志的方法，包含以下属性：
  - value: 操作ID
  - module: 模块名
  - type: 操作类型

### 切面实现
- OperationLoggerAdvice: 拦截@OperationLog注解方法，记录操作日志

### 服务接口
- OperationService: 操作日志服务接口
- DefaultOperationServiceImpl: 默认实现(打印到控制台)

### 数据模型
- OperationBody: 操作日志主体，包含操作ID、方法、模块、耗时等
- OperationDetail: 操作详情，包含请求地址、参数、消息体和结果

## 配置方式
```properties
# 启用/禁用操作日志功能
lambda.logging.enabled=true

# Kafka日志收集配置(可选)
lambda.logging.kafka.enabled=false
lambda.logging.kafka.topic=operation-logs
```

## 使用示例
```java
@OperationLog(value = "user.create", module = "用户管理", type = "CREATE")
@PostMapping("/users")
public User createUser(@RequestBody User user) {
    return userService.create(user);
}
```

## 实现说明
1. 默认实现仅打印日志到控制台
2. 可通过实现OperationService接口自定义日志存储
3. 日志记录包含完整的方法执行上下文
4. 支持通过配置启用Kafka日志收集
