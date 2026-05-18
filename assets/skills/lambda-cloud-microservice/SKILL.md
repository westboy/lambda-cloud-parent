---
name: "lambda-cloud-microservice"
description: "Lambda Cloud 微服务全景配置指南。当用户需要搭建完整微服务架构、配置Nacos+Gateway+Dubbo/Feign全链路或实现多租户方案时调用。"
---

# Lambda Cloud 微服务全景

## 架构总览

```
客户端
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│  API Gateway (lambda-cloud-starter-gateway)                      │
│  ├─ 防火墙鉴权 (Sa-Token)                                         │
│  ├─ CORS 跨域                                                    │
│  ├─ 租户路由改写                                                  │
│  ├─ Swagger 文档聚合                                              │
│  └─ 限流/熔断                                                    │
└───────────────┬─────────────────────────────────────────────────┘
                │
    ┌───────────┼───────────┐
    │           │           │
    ▼           ▼           ▼
┌────────┐ ┌────────┐ ┌────────┐
│ 用户服务 │ │ 订单服务 │ │ 支付服务 │
│        │ │        │ │        │
│ Dubbo  │ │ Dubbo  │ │ Dubbo  │
│ Feign  │ │ Feign  │ │ Feign  │
└───┬────┘ └───┬────┘ └───┬────┘
    │          │          │
    └──────────┼──────────┘
               │
    ┌──────────┼──────────┐
    │          │          │
    ▼          ▼          ▼
┌──────┐  ┌──────┐  ┌──────┐
│MySQL │  │Redis │  │ MQ   │
└──────┘  └──────┘  └──────┘

注册中心: Nacos (服务发现 + 配置中心)
```

## 模块组合

### Gateway 网关

```xml
<dependencies>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-nacos</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-redis</artifactId>
    </dependency>
</dependencies>
```

### 微服务提供者

```xml
<dependencies>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-mybatis</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-dubbo</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-feign</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-nacos</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>com.lambda.cloud</groupId>
        <artifactId>lambda-cloud-starter-swagger</artifactId>
    </dependency>
</dependencies>
```

## Nacos 配置

### 服务发现

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
```

### 配置中心

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
        file-extension: yaml
        shared-configs:
          - data-id: common.yaml
            group: DEFAULT_GROUP
            refresh: true
```

## Gateway 配置

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
          metadata:
            api-docs: /v3/api-docs
            api-name: 用户服务
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
          metadata:
            api-docs: /v3/api-docs
            api-name: 订单服务

lambda:
  web:
    firewall:
      enabled: true
      whites:
        - /public/**
        - /actuator/**
    cors:
      enabled: true
      allowed-origins:
        - http://localhost:3000
  security:
    sa-token:
      timeout: 86400
      check-same-token: true
```

## 服务提供者配置

### application.yaml

```yaml
spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yaml
  datasource:
    url: jdbc:mysql://localhost:3306/user_db
    username: root
    password: root

mybatis-plus:
  mapper-package: com.your.company.user.mapper
  tenant:
    enabled: true
    tenant-column: tenant_id

lambda:
  security:
    form:
      enabled: true
  dubbo:
    security:
      enabled: true
    tenant:
      enabled: true
  api-docs:
    title: 用户服务 API
    enabled: true
```

## Dubbo 服务定义与消费

### 服务接口 (API 模块)

```java
public interface UserService {

    UserVO getUserById(Long id);

    UserVO createUser(UserCreateDTO dto);

    List<UserVO> listUsers();
}
```

### 服务实现

```java
@DubboService
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserVO getUserById(Long id) {
        UserEntity entity = userMapper.selectById(id);
        return BaseVO.fromEntity(UserVO.class, entity);
    }

    @Override
    @Transactional
    public UserVO createUser(UserCreateDTO dto) {
        UserEntity entity = dto.toEntity();
        userMapper.insert(entity);
        return BaseVO.fromEntity(UserVO.class, entity);
    }

    @Override
    public List<UserVO> listUsers() {
        List<UserEntity> entities = userMapper.selectList(null);
        return BaseVO.fromEntityList(UserVO.class, entities);
    }
}
```

### 服务消费

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @DubboReference
    private UserService userService;

    @GetMapping("/{id}")
    public UserVO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public UserVO createUser(@RequestBody UserCreateDTO dto) {
        return userService.createUser(dto);
    }
}
```

## Feign 调用

### Feign 客户端

```java
@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderFeignClient {

    @GetMapping("/user/{userId}")
    List<OrderVO> getOrdersByUserId(@PathVariable("userId") Long userId);

    @PostMapping
    OrderVO createOrder(@RequestBody OrderCreateDTO dto);
}
```

### 使用 Feign

```java
@Service
public class UserOrderService {

    @Autowired
    private OrderFeignClient orderFeignClient;

    public List<OrderVO> getUserOrders(Long userId) {
        return orderFeignClient.getOrdersByUserId(userId);
    }
}
```

## 多租户全链路方案

### 1. Gateway 路由改写

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: tenant-service
          uri: lb://tenant-service
          predicates:
            - Path=/api/tenant/**
          filters:
            - name: TenantRouteRewriter
              args:
                tenantIdHeader: X-Tenant-Id
```

### 2. Dubbo/Feign 上下文透传

```yaml
lambda:
  dubbo:
    tenant:
      enabled: true
      tenant-id-header: X-Tenant-Id
```

### 3. MyBatis 行级隔离

```yaml
mybatis-plus:
  tenant:
    enabled: true
    tenant-column: tenant_id
    ignore-tables:
      - sys_tenant
      - sys_config
```

### 4. 获取租户上下文

```java
// 从当前操作人获取
String tenantId = OperatorUtils.getSafeOperator().getTenantId();

// 手动设置
TenantContextHolder.setTenantId("tenant001");

// 从 Dubbo 上下文获取
String tenantId = DubboContextHolder.getTenantId();
```

## 统一监控

### Actuator + Micrometer

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Dubbo 健康检查

```yaml
lambda:
  dubbo:
    monitoring:
      enabled: true
      enable-metrics: true
      enable-logging: true
```

## 完整微服务配置示例

```yaml
spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
      config:
        server-addr: localhost:8848
        namespace: dev
        file-extension: yaml
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/user_master
          username: root
          password: root
        slave:
          url: jdbc:mysql://localhost:3306/user_slave
          username: readonly
          password: readonly
  data:
    redis:
      host: localhost
      port: 6379

mybatis-plus:
  mapper-package: com.your.company.user.mapper
  tenant:
    enabled: true
    tenant-column: tenant_id

lambda:
  web:
    cors:
      enabled: true
  security:
    form:
      enabled: true
  dubbo:
    security:
      enabled: true
    tenant:
      enabled: true
  cache:
    type: MULTI_LEVEL
  api-docs:
    title: 用户服务 API
    enabled: true
  logging:
    operation:
      enabled: true
```

## 最佳实践

1. **服务拆分**: 按业务领域拆分微服务, 避免过大或过小
2. **接口定义**: API 模块独立, 供其他服务引用
3. **配置管理**: 公共配置放 Nacos, 敏感配置用加密
4. **服务治理**: 开启 Dubbo 监控和健康检查
5. **链路追踪**: 结合 SkyWalking 或 Zipkin 实现分布式追踪
6. **限流熔断**: Gateway 层配置限流, Dubbo/Feign 配置熔断
7. **日志聚合**: 统一操作日志采集, 方便问题排查
