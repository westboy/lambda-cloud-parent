# Lambda Cloud Dubbo Starter

[![Maven Central](https://img.shields.io/maven-central/v/com.lambda.cloud/lambda-cloud-starter-dubbo.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.lambda.cloud%22%20AND%20a:%22lambda-cloud-starter-dubbo%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Dubbo](https://img.shields.io/badge/Dubbo-3.3.5-orange.svg)](https://dubbo.apache.org/)

Lambda Cloud Dubbo Starter 是基于 Apache Dubbo 3.3.5 的企业级增强封装，为 Spring Boot 应用提供开箱即用的 Dubbo 企业级功能。

## ✨ 特性概览

### 🔒 安全认证
- **认证上下文传播** - 自动传播认证令牌、用户ID和租户ID
- **安全过滤器** - 统一的认证信息处理

### 📊 监控可观测性
- **性能指标收集** - 请求次数、响应时间、成功率统计
- **健康检查集成** - Spring Boot Actuator 健康检查端点
- **慢调用检测** - 可配置的慢调用阈值和告警
- **结构化日志** - 详细的请求/响应日志记录

### ⚡ 智能重试
- **指数退避算法** - 智能的重试间隔策略
- **可配置异常类型** - 只对指定异常进行重试
- **重试次数控制** - 灵活的重试次数和间隔配置

### 🏢 多租户支持
- **租户上下文隔离** - 完整的多租户数据隔离
- **上下文自动传播** - 跨服务的租户信息传递
- **默认租户配置** - 兜底的租户处理机制

### 🛠️ 开发友好
- **配置验证** - 启动时自动验证配置合法性
- **Mock服务支持** - 开发测试期间的服务模拟
- **丰富的配置选项** - 细粒度的功能开关控制

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-dubbo</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 基础配置

```yaml
# application.yml
spring:
  application:
    name: your-service-name

# 标准 Dubbo 配置
dubbo:
  application:
    name: ${spring.application.name}
  registry:
    address: nacos://localhost:8848
  protocol:
    name: dubbo
    port: -1

# Lambda Dubbo 增强配置
lambda:
  dubbo:
    # 安全认证
    security:
      enabled: true
      token-header: Authorization
      user-header: X-User-Id
      tenant-header: X-Tenant-Id
    
    # 监控配置
    monitoring:
      enabled: true
      enable-metrics: true
      enable-logging: true
      slow-call-threshold: 1000
    
    # 重试配置
    retry:
      enabled: true
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2.0
```

### 3. 使用服务

#### 服务提供者

```java
@DubboService
public class UserServiceImpl implements UserService {
    
    @Override
    public User getUserById(Long userId) {
        // 通过工具类获取上下文信息
        String currentTenantId = DubboContextHolder.getCurrentTenantId();
        String currentUserId = DubboContextHolder.getCurrentUserId();
        
        // 业务逻辑...
        return userRepository.findById(userId);
    }
}
```

#### 服务消费者

```java
@RestController
public class UserController {
    
    @DubboReference
    private UserService userService;
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        // 设置上下文信息（可选，通常由过滤器自动处理）
        DubboContextHolder.setCurrentTenantId("tenant-001");
        DubboContextHolder.setCurrentUserId("user-123");
        
        return userService.getUserById(id);
    }
}
```

## ⚙️ 配置参考

### 完整配置示例

```yaml
lambda:
  dubbo:
    # 安全认证配置
    security:
      enabled: true                    # 是否启用安全认证
      token-header: Authorization      # 认证令牌请求头
      user-header: X-User-Id          # 用户ID请求头
      tenant-header: X-Tenant-Id      # 租户ID请求头
    
    # 监控和可观测性配置
    monitoring:
      enabled: true                   # 是否启用监控
      enable-metrics: true            # 是否启用性能指标收集
      enable-logging: true            # 是否启用请求日志
      slow-call-threshold: 1000       # 慢调用阈值(毫秒)
    
    # 重试机制配置
    retry:
      enabled: true                   # 是否启用重试
      max-attempts: 3                 # 最大重试次数
      initial-interval: 1000          # 初始重试间隔(毫秒)
      multiplier: 2.0                 # 重试间隔倍数
      max-interval: 10000             # 最大重试间隔(毫秒)
      retryable-exceptions:           # 可重试的异常类型
        - java.util.concurrent.TimeoutException
        - java.net.SocketTimeoutException
    
    # 多租户配置
    tenant:
      enabled: false                  # 是否启用多租户
      tenant-id-header: X-Tenant-Id   # 租户ID请求头
      default-tenant: default         # 默认租户ID
      inherit-tenant-context: true    # 是否继承租户上下文
```

## 📊 监控指标

### 健康检查端点

访问 `GET /actuator/health` 查看Dubbo服务健康状态：

```json
{
  "status": "UP",
  "components": {
    "dubbo": {
      "status": "UP",
      "details": {
        "message": "All Dubbo services are healthy",
        "totalServices": 2,
        "UserService.getUserById": {
          "status": "HEALTHY",
          "successRate": "99.50%",
          "totalRequests": 1000,
          "averageDuration": "45ms",
          "maxDuration": "200ms"
        }
      }
    }
  }
}
```

### 性能指标

通过 `DubboMetricsCollector` 获取详细的性能统计：

```java
@RestController
public class MetricsController {
    
    @Autowired
    private DubboMetricsCollector metricsCollector;
    
    @GetMapping("/metrics/dubbo")
    public Map<String, ?> getDubboMetrics() {
        return metricsCollector.getAllMetrics();
    }
}
```

## 🔧 高级用法

### 自定义过滤器

```java
@Component
public class CustomDubboFilter implements Filter {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 自定义逻辑
        return invoker.invoke(invocation);
    }
}
```

### 扩展配置

```java
@Configuration
@ConditionalOnProperty(value = "lambda.dubbo.custom.enabled", havingValue = "true")
public class CustomDubboConfiguration {
    
    @Bean
    public CustomDubboComponent customDubboComponent() {
        return new CustomDubboComponent();
    }
}
```

## 🔌 与 Lambda Cloud 生态集成

Lambda Cloud Dubbo Starter 与其他 Lambda Cloud 组件无缝集成：

- **lambda-cloud-starter-redis** - 分布式缓存支持
- **lambda-cloud-starter-mybatis** - 数据访问层集成
- **lambda-cloud-starter-security** - 统一安全认证
- **lambda-cloud-starter-gateway** - API网关集成

## 📋 兼容性

| 组件 | 版本要求 |
|------|---------|
| Spring Boot | 3.5.3+ |
| Apache Dubbo | 3.3.5 |
| Java | 21+ |
| Nacos (可选) | 2.x |

## 🤝 贡献指南

我们欢迎社区贡献！请查看 [贡献指南](CONTRIBUTING.md) 了解如何参与项目开发。

### 开发环境搭建

```bash
# 克隆项目
git clone https://github.com/lambda-cloud/lambda-cloud-parent.git

# 进入项目目录
cd lambda-cloud-parent/lambda-cloud-starter-dubbo

# 编译项目
mvn clean compile

# 运行测试
mvn test
```

## 📄 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源协议。

## 📞 支持与反馈

- **问题反馈**: [GitHub Issues](https://github.com/lambda-cloud/lambda-cloud-parent/issues)
- **功能建议**: [GitHub Discussions](https://github.com/lambda-cloud/lambda-cloud-parent/discussions)
- **技术交流**: [加入社区群组](#)

---

⭐ 如果这个项目对你有帮助，请给我们一个 Star！