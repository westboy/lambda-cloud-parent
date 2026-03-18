# lambda-cloud-starter-redis

`lambda-cloud-starter-redis` 提供 Redis 访问基础能力，包含统一模板序列化配置、Lettuce 连接保活优化、Redisson 连接工厂桥接，以及可复用的 Redis 工具与延迟队列组件。

## 模块定位

- 统一输出 Redis 访问模板与序列化策略。
- 解决 Lettuce 长连接空闲场景下的间歇性超时问题。
- 提供 `RedisHelper` 封装，覆盖常用 Key/String/Hash/List/Set/ZSet 操作。
- 提供基于 Redisson 的延迟队列执行组件（需业务显式装配）。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
└─RedisAutoConfiguration.java

src/main/java/com/lambda/cloud/redis/
├─ helper/RedisHelper.java
├─ listener/
│  ├─ RedisKeyExpiredListener.java
│  └─ KeyExpiredEventMessageListener.java
└─ delay/
   ├─ RedisDelayConfig.java
   ├─ RedisDelayedListener.java
   ├─ RedisDelayedQueueManager.java
   └─ RedisDelayedWorker.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.RedisAutoConfiguration
```

## 自动装配机制

`RedisAutoConfiguration` 输出核心 Bean：

- `RedisHelper`
- `LettuceClientConfigurationBuilderCustomizer`
- `ClientResourcesBuilderCustomizer`
- `ObjectMapper`（Bean 名：`jacksonJsonMapper`，缺省时创建）
- `jdkRedisTemplate`
- `stringRedisTemplate`
- `redisTemplate`（POJO 模板）
- `RedissonConnectionFactory`（仅缺失 `RedisConnectionFactory` 时创建）

### Lettuce 连接优化

- 启用 `SO_KEEPALIVE` 配置。
- 读策略固定为 `ReadFrom.MASTER`。
- 对连接 channel 注入 `IdleStateHandler(30s)`。
- 触发 `ALL_IDLE` 时主动 `disconnect`，用于避免长空闲连接导致的超时粘连问题。

## RedisTemplate 策略

### jdkRedisTemplate

- Key/HashKey：`StringRedisSerializer`
- Value/HashValue：沿用 `RedisTemplate` 默认序列化

### stringRedisTemplate

- Key：`StringRedisSerializer`
- Value/HashValue：`GenericJacksonJsonRedisSerializer`
- 事务支持：关闭（`setEnableTransactionSupport(false)`）

### redisTemplate（POJO）

- Key：`StringRedisSerializer`
- Value/HashValue：`GenericJacksonJsonRedisSerializer`
- 事务支持：关闭

序列化 ObjectMapper：

- 使用 `JsonInclude.Include.NON_NULL`
- Bean 名固定 `jacksonJsonMapper`

## 配置说明

### spring.data.redis

连接地址、密码、数据库等基础配置由 Spring Data Redis 标准配置承接。

## RedisHelper 能力

`RedisHelper` 基于 `RedisTemplate<String, Object>` 封装，覆盖：

- Key 管理：删除、过期、重命名、类型、TTL
- String：set/get、bit、multiGet/multiSet、自增
- Hash：hGet/hPut/hScan 等
- List：push/pop、阻塞 pop、trim、长度
- Set：交并差、随机成员、scan
- ZSet：rank/score/range、交并存储、scan

适用场景：

- 统一业务层调用风格，减少直接操作 `opsForXxx()` 的重复样板代码。

## 延迟队列能力

提供通用组件（非自动装配）：

- `RedisDelayConfig`：队列名、默认延迟、工作线程数等
- `RedisDelayedQueueManager<T>`：延迟队列管理器（`CommandLineRunner`）
- `RedisDelayedWorker<T>`：消费工作线程
- `RedisDelayedListener<T>`：业务回调接口

运行机制：

1. `afterPropertiesSet()` 获取 `RBlockingQueue` 与 `RDelayedQueue`
2. `run()` 启动定时调度，每 `100ms` 拉取队列
3. `RedisDelayedWorker` 使用固定大小线程池分发 `listener.execute(obj)`

使用前提：

- 需要容器中存在 `RedissonClient`
- 不存在时会抛 `NotSupportedException`，提示开启 `spring.data.redis.redisson.enabled`

## Key 过期事件监听

提供接口与桥接类：

- `RedisKeyExpiredListener`
- `KeyExpiredEventMessageListener`

说明：

- 本模块未自动注册 `RedisMessageListenerContainer` 与该监听器，需业务侧自行装配。
- 监听器将 Redis Message 转换为 `RedisKeyExpiredEvent<String>` 并回调业务 `publisher`。

## 依赖说明

关键依赖（见 `pom.xml`）：

- `spring-boot-starter-data-redis`
- `org.redisson:redisson-spring-data-40`
- `org.redisson:redisson-spring-boot-starter`
- `org.apache.commons:commons-pool2`
- `io.netty:netty-transport-native-epoll`
- `com.lambda.cloud:lambda-cloud-core`

## 当前实现约束

- `stringRedisTemplate` Bean 名与 Spring 默认同名，若业务自定义同名 Bean 需注意覆盖关系。
- 延迟队列组件不是自动配置能力，必须手工声明 `RedisDelayConfig`、监听器与管理器 Bean。
- `RedisDelayedQueueManager#destroy()` 当前仅调用 `isShutdown()`，不负责关闭 `RedissonClient`。
- `RedisHelper` 是宽接口封装，复杂事务/流水线场景仍建议直接使用底层模板 API。
