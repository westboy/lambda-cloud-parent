# Lambda Cloud Starter Nacos

Nacos 服务发现和配置管理的 Spring Boot Starter，整合了 Alibaba Nacos 相关依赖。

## 功能特性

- 集成 Nacos 服务发现
- 集成 Nacos 配置管理
- 统一依赖管理，避免版本冲突
- 排除不必要的日志适配器

## 依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-nacos</artifactId>
</dependency>
```

## 包含的依赖

该 Starter 整合了以下 Nacos 相关依赖：

- `nacos-client` - Nacos 客户端核心库
- `spring-cloud-starter-alibaba-nacos-discovery` - Nacos 服务发现
- `spring-cloud-starter-alibaba-nacos-config` - Nacos 配置管理

## 配置示例

### 服务发现配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
        cluster-name: DEFAULT
        service: ${spring.application.name}
        weight: 1
        ip: 192.168.1.100
        port: ${server.port}
        secure: false
        access-key: your-access-key
        secret-key: your-secret-key
        log-name: nacos-discovery.log
        endpoint: your-endpoint
```

### 配置管理配置

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
        name: ${spring.application.name}
        file-extension: yaml
        timeout: 3000
        max-retry: 10
        config-long-poll-timeout: 46000
        config-retry-time: 2333
        enable-remote-sync-config: true
        access-key: your-access-key
        secret-key: your-secret-key
        endpoint: your-endpoint
```

### 多配置文件

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        extension-configs:
          - data-id: common.yaml
            group: COMMON_GROUP
            refresh: true
          - data-id: redis.yaml
            group: MIDDLEWARE_GROUP
            refresh: true
        shared-configs:
          - data-id: shared.yaml
            group: SHARED_GROUP
            refresh: true
```

## 使用示例

### 基本使用

1. 添加依赖到项目
2. 配置 Nacos 服务器地址
3. 启动应用，自动注册到 Nacos

### 服务发现

```java
@RestController
public class DiscoveryController {
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }
    
    @GetMapping("/instances/{service}")
    public List<ServiceInstance> getInstances(@PathVariable String service) {
        return discoveryClient.getInstances(service);
    }
}
```

### 配置动态刷新

```java
@RestController
@RefreshScope
public class ConfigController {
    
    @Value("${user.name:default}")
    private String userName;
    
    @Value("${user.age:0}")
    private Integer userAge;
    
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("userName", userName);
        config.put("userAge", userAge);
        return config;
    }
}
```

## 注意事项

- 确保 Nacos 服务器正常运行
- 配置正确的服务器地址和端口
- 注意命名空间和分组的配置
- 使用 `@RefreshScope` 注解实现配置动态刷新
- 该 Starter 已排除冲突的日志适配器依赖