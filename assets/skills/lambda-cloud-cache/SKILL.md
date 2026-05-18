---
name: "lambda-cloud-cache"
description: "Lambda Cloud 缓存与Redis配置指南。当用户需要配置Redis、使用缓存(@Cacheable)、多级缓存、延迟队列或RedisHelper工具时调用。"
---

# Lambda Cloud 缓存与 Redis

## 模块引入

```xml
<!-- Redis 基础 -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-redis</artifactId>
</dependency>

<!-- 统一缓存抽象 (可选, 支持多级缓存) -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-cache</artifactId>
</dependency>
```

## Redis 基础配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your-password
      database: 0
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 4
```

### 自动装配的 Bean

| Bean | 说明 |
|------|------|
| `redisTemplate` | POJO 模板, Key=String, Value=Jackson JSON |
| `stringRedisTemplate` | 字符串模板, Key/Value 均为 String |
| `jdkRedisTemplate` | JDK 序列化模板 |
| `RedisHelper` | 通用操作封装 |
| `RedissonConnectionFactory` | Redisson 连接工厂 |

## 统一缓存配置

配置前缀: `lambda.cache`

### 三种缓存模式

CacheType 枚举定义了三种缓存模式：

```java
public enum CacheType {
    REDIS("redis", "Redis分布式缓存"),
    CAFFEINE("caffeine", "Caffeine本地缓存"),
    MULTI_LEVEL("multi-level", "多级缓存");
}
```

#### 1. REDIS 模式 (默认)

```yaml
lambda:
  cache:
    type: REDIS
    defaults:
      ttl: PT1H  # Duration 类型，ISO-8601 格式
      key-prefix: "cache:"
      allow-null-values: true
      enable-stats: true
    caches:
      user-cache:
        ttl: PT2H
        key-prefix: "user:"
```

#### 2. CAFFEINE 模式 (本地缓存)

```yaml
lambda:
  cache:
    type: CAFFEINE
    defaults:
      max-size: 10000
      initial-capacity: 100
      expire-after-write: PT5M  # Duration 类型
      expire-after-access: PT1M  # Duration 类型
      refresh-after-write: PT1M  # Duration 类型
      allow-null-values: true
      enable-stats: true
      soft-values: false  # 软引用值
      weak-values: false  # 弱引用值
      weak-keys: false  # 弱引用键
```

#### 3. MULTI_LEVEL 模式 (L1 + L2)

```yaml
lambda:
  cache:
    type: MULTI_LEVEL
    defaults:
      ttl: PT1H        # L2 过期时间，Duration 类型
      l1-ttl: PT5M     # L1 过期时间，Duration 类型
      max-size: 5000
      initial-capacity: 100
      allow-null-values: true
      enable-stats: true
    caches:
      user-cache:
        ttl: PT2H
        l1-ttl: PT10M
```

### 多级缓存工作机制

- **读流程**: L1 (Caffeine) → L2 (Redis) → 回填 L1
- **写流程**: 同时写 L2 和 L1, 发布同步消息
- **删流程**: 同时删 L2 和 L1, 发布同步消息
- **跨节点同步**: Redis Pub/Sub 广播失效消息

## @Cacheable 使用

```java
@Service
public class UserService {

    @Cacheable(value = "user-cache", key = "#userId")
    public UserVO getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @CachePut(value = "user-cache", key = "#user.id")
    public UserVO updateUser(UserEntity user) {
        userMapper.updateById(user);
        return BaseVO.fromEntity(UserVO.class, user);
    }

    @CacheEvict(value = "user-cache", key = "#userId")
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    @CacheEvict(value = "user-cache", allEntries = true)
    public void clearCache() {
        // 清除所有缓存
    }
}
```

## RedisHelper 工具类

基于 `RedisTemplate<String, Object>` 封装, 提供常用操作:

### String 操作

```java
@Autowired
private RedisHelper redisHelper;

// 设置值
redisHelper.set("key", "value");
redisHelper.set("key", "value", 3600, TimeUnit.SECONDS);

// 获取值
Object value = redisHelper.get("key");
String strValue = redisHelper.get("key", String.class);

// 自增
Long count = redisHelper.increment("counter");
Long count = redisHelper.increment("counter", 10);

// 批量操作
redisHelper.multiSet(Map.of("k1", "v1", "k2", "v2"));
List<Object> values = redisHelper.multiGet(Arrays.asList("k1", "k2"));
List<String> strValues = redisHelper.multiGet(Arrays.asList("k1", "k2"), String.class);
```

### Hash 操作

```java
// 设置 hash 值
redisHelper.hPut("user:1", "name", "张三");
redisHelper.hPut("user:1", "age", 25);

// 批量设置 hash 值
redisHelper.hPutAll("user:1", Map.of("name", "张三", "age", 25));

// 获取 hash 值
Object name = redisHelper.hGet("user:1", "name");
String strName = redisHelper.hGet("user:1", "name", String.class);
Map<Object, Object> entries = redisHelper.hGetAll("user:1");
Map<String, String> strEntries = redisHelper.hGetAll("user:1", String.class);

// 删除 hash 值
redisHelper.hDelete("user:1", "name");

// 判断 hash 字段是否存在
Boolean exists = redisHelper.hHasKey("user:1", "name");

// 获取 hash 字段数量
Long size = redisHelper.hSize("user:1");

// hash 字段自增
Double newValue = redisHelper.hIncrement("user:1", "age", 1);
```

### List 操作

```java
// 左推入
redisHelper.lLeftPush("queue", "task1");
redisHelper.lLeftPushAll("queue", Arrays.asList("task1", "task2"));
redisHelper.lLeftPushAll("queue", "task1", "task2");

// 右推入
redisHelper.lRightPush("queue", "task1");
redisHelper.lRightPushAll("queue", Arrays.asList("task1", "task2"));
redisHelper.lRightPushAll("queue", "task1", "task2");

// 左弹出
Object task = redisHelper.lLeftPop("queue");
String strTask = redisHelper.lLeftPop("queue", String.class);

// 右弹出
Object task = redisHelper.lRightPop("queue");
String strTask = redisHelper.lRightPop("queue", String.class);

// 阻塞左弹出
Object task = redisHelper.lBLeftPop("queue", 10, TimeUnit.SECONDS);
String strTask = redisHelper.lBLeftPop("queue", 10, TimeUnit.SECONDS, String.class);

// 阻塞右弹出
Object task = redisHelper.lBRightPop("queue", 10, TimeUnit.SECONDS);
String strTask = redisHelper.lBRightPop("queue", 10, TimeUnit.SECONDS, String.class);

// 获取列表
List<Object> tasks = redisHelper.lRange("queue", 0, -1);
List<String> strTasks = redisHelper.lRange("queue", 0, -1, String.class);

// 获取指定索引的元素
Object task = redisHelper.lIndex("queue", 0);
String strTask = redisHelper.lIndex("queue", 0, String.class);

// 获取列表长度
Long size = redisHelper.lSize("queue");

// 设置指定索引的元素
redisHelper.lSet("queue", 0, "newTask");

// 移除元素
redisHelper.lRemove("queue", 1, "task1");
```

### Set 操作

```java
// 添加成员
redisHelper.sAdd("tags", "java", "spring", "redis");

// 获取所有成员
Set<Object> tags = redisHelper.setMembers("tags");
Set<String> strTags = redisHelper.setMembers("tags", String.class);

// 判断成员是否存在
boolean exists = redisHelper.sIsMember("tags", "java");

// 移除成员
redisHelper.sRemove("tags", "java");

// 随机弹出成员
Object tag = redisHelper.sPop("tags");
String strTag = redisHelper.sPop("tags", String.class);

// 集合运算
Set<Object> diff = redisHelper.sDifference("set1", "set2");
Set<String> strDiff = redisHelper.sDifference("set1", "set2", String.class);
Set<Object> union = redisHelper.sUnion("set1", "set2");
Set<String> strUnion = redisHelper.sUnion("set1", "set2", String.class);
Set<Object> inter = redisHelper.sIntersect("set1", "set2");
Set<String> strInter = redisHelper.sIntersect("set1", "set2", String.class);

// 集合运算并存储结果
redisHelper.sDifferenceAndStore("set1", "set2", "destSet");
redisHelper.sUnionAndStore("set1", "set2", "destSet");
redisHelper.sIntersectAndStore("set1", "set2", "destSet");

// 获取集合大小
Long size = redisHelper.sSize("tags");
```

### ZSet 操作

```java
// 添加成员
redisHelper.zAdd("ranking", "user1", 100.0);
redisHelper.zAdd("ranking", Set.of(TypedTuple.of("user1", 100.0), TypedTuple.of("user2", 90.0)));

// 移除成员
redisHelper.zRemove("ranking", "user1");

// 获取排名
Long rank = redisHelper.zRank("ranking", "user1");
Long reverseRank = redisHelper.zReverseRank("ranking", "user1");

// 获取分数
Double score = redisHelper.zScore("ranking", "user1");

// 增加分数
Double newScore = redisHelper.zIncrementScore("ranking", "user1", 10.0);

// 范围查询
Set<Object> topN = redisHelper.zRange("ranking", 0, 9);
Set<String> strTopN = redisHelper.zRange("ranking", 0, 9, String.class);
Set<Object> topNWithScores = redisHelper.zRangeWithScores("ranking", 0, 9);
Set<TypedTuple<Object>> topNTuples = redisHelper.zRangeWithScores("ranking", 0, 9);

// 按分数范围查询
Set<Object> range = redisHelper.zRangeByScore("ranking", 0, 100);
Set<String> strRange = redisHelper.zRangeByScore("ranking", 0, 100, String.class);
Set<Object> rangeWithScores = redisHelper.zRangeByScoreWithScores("ranking", 0, 100, 0, 10);
Set<TypedTuple<Object>> rangeTuples = redisHelper.zRangeByScoreWithScores("ranking", 0, 100, 0, 10);

// 反向范围查询
Set<Object> reverseRange = redisHelper.zReverseRange("ranking", 0, 9);
Set<String> strReverseRange = redisHelper.zReverseRange("ranking", 0, 9, String.class);
Set<Object> reverseRangeByScore = redisHelper.zReverseRangeByScore("ranking", 0, 100);
Set<String> strReverseRangeByScore = redisHelper.zReverseRangeByScore("ranking", 0, 100, String.class);

// 获取集合大小
Long size = redisHelper.zSize("ranking");

// 获取分数范围内的成员数量
Long count = redisHelper.zCount("ranking", 0, 100);

// 按索引范围移除成员
redisHelper.zRemoveRange("ranking", 0, 9);

// 按分数范围移除成员
redisHelper.zRemoveRangeByScore("ranking", 0, 100);

// 集合并集并存储
redisHelper.zUnionAndStore("set1", "set2", "destSet");

// 集合交集并存储
redisHelper.zIntersectionAndStore("set1", "set2", "destSet");

// 随机获取成员
Object member = redisHelper.randomMember("ranking");
String strMember = redisHelper.randomMember("ranking", String.class);
Set<Object> distinctMembers = redisHelper.distinctRandomMembers("ranking", 3);
Set<String> strDistinctMembers = redisHelper.distinctRandomMembers("ranking", 3, String.class);
Set<Object> members = redisHelper.randomMembers("ranking", 3);
Set<String> strMembers = redisHelper.randomMembers("ranking", 3, String.class);
```

### Key 管理

```java
// 删除
redisHelper.delete("key");
redisHelper.delete(Arrays.asList("key1", "key2"));

// 设置过期
redisHelper.expire("key", 3600, TimeUnit.SECONDS);

// 获取 TTL
Long ttl = redisHelper.getExpire("key");
Long ttlSeconds = redisHelper.getExpire("key", TimeUnit.SECONDS);

// 判断存在
boolean exists = redisHelper.hasKey("key");

// 获取类型
String type = redisHelper.type("key");

// 重命名
redisHelper.rename("oldKey", "newKey");

// 获取所有匹配的 key
Set<String> keys = redisHelper.keys("user:*");

// 随机获取一个 key
String randomKey = redisHelper.randomKey();
```

## 延迟队列

基于 Redisson 的延迟队列组件, 需手动装配:

### 配置类

```java
@Configuration
public class DelayQueueConfig {

    @Bean
    public RedisDelayConfig orderDelayConfig() {
        RedisDelayConfig config = new RedisDelayConfig();
        config.setQueueName("order-delay-queue");
        config.setDefaultDelay(30); // 默认延迟 30 秒
        config.setWorkerThreads(4);
        return config;
    }

    @Bean
    public RedisDelayedQueueManager<OrderMessage> orderDelayManager(
            RedisDelayConfig orderDelayConfig) {
        return new RedisDelayedQueueManager<>(orderDelayConfig);
    }

    @Bean
    public RedisDelayedListener<OrderMessage> orderDelayListener() {
        return new RedisDelayedListener<OrderMessage>() {
            @Override
            public void execute(OrderMessage message) {
                // 处理延迟消息
                orderService.handleTimeout(message.getOrderId());
            }
        };
    }
}
```

### 使用延迟队列

```java
@Service
public class OrderService {

    @Autowired
    private RedisDelayedQueueManager<OrderMessage> orderDelayManager;

    public void createOrder(OrderEntity order) {
        // 保存订单
        orderMapper.insert(order);

        // 发送延迟消息 (30 秒后检查支付状态)
        OrderMessage message = new OrderMessage(order.getId());
        orderDelayManager.offer(message, 30, TimeUnit.SECONDS);
    }
}
```

## Key 过期事件监听

需手动装配:

```java
@Configuration
public class RedisListenerConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    public RedisKeyExpiredListener redisKeyExpiredListener(
            RedisMessageListenerContainer container) {
        return new RedisKeyExpiredListener(container) {
            @Override
            public void onMessage(byte[] message, byte[] pattern) {
                String expiredKey = new String(message);
                // 处理 Key 过期事件
            }
        };
    }
}
```

## 最佳实践

1. **Key 命名**: 使用 `业务:对象:ID` 格式, 如 `user:info:123`
2. **过期时间**: 所有缓存 Key 都应设置过期时间, 避免内存泄漏
3. **序列化**: 使用 Jackson JSON 序列化, 避免 JDK 序列化的安全风险
4. **大对象**: 超过 1MB 的对象不建议放入 Redis
5. **多级缓存**: 热点数据使用 MULTI_LEVEL 模式, 减少 Redis 压力
6. **延迟队列**: 确保 Redisson 连接正常, 否则会抛 NotSupportedException
