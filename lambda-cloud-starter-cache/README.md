# lambda-cloud-starter-cache

`lambda-cloud-starter-cache` 提供统一缓存自动配置，支持三种模式：

- `REDIS`：基于 `RedisCacheManager`
- `CAFFEINE`：基于 `CaffeineCacheManager`
- `MULTI_LEVEL`：L1 Caffeine + L2 Redis 的两级缓存

## 模块定位

- 统一 Spring Cache 体系的默认接入方式，减少业务重复配置。
- 在多级缓存模式下，提供跨节点 L1 失效同步能力（Redis Pub/Sub）。
- 对外暴露标准 `org.springframework.cache.CacheManager`，兼容 `@Cacheable` 体系。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ CacheAutoConfiguration.java
└─ CacheProperties.java

src/main/java/com/lambda/cloud/cache/
├─ CacheConfig.java
├─ CacheConstants.java
├─ CacheType.java
├─ provider/
│  ├─ MultiLevelCache.java
│  └─ MultiLevelCacheManager.java
└─ support/
   ├─ CacheMessage.java
   ├─ CacheMessageListener.java
   └─ CaffeineFactory.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.CacheAutoConfiguration
```

## 自动装配机制

### 总开关

- 配置键：`lambda.cache.enabled`
- 条件：`havingValue=true, matchIfMissing=true`
- 结论：默认启用缓存自动配置

### 按类型装配

### CAFFEINE

- 条件：
  - `lambda.cache.type=CAFFEINE`
  - classpath 存在 `com.github.benmanes.caffeine.cache.Caffeine`
- 输出：
  - `CaffeineCacheManager`
- 特点：
  - 支持 `defaults` + `caches` 细粒度配置
  - `allowNullValues` 按配置生效

### REDIS

- 条件：
  - `lambda.cache.type=REDIS`（`matchIfMissing=true`）
  - classpath 存在 `RedisTemplate`
- 输出：
  - `RedisCacheManager`
- 特点：
  - Key 序列化固定为 String
  - Value 序列化固定为 `RedisSerializer.json()`
  - 支持 per-cache TTL/前缀/null-value 配置

### MULTI_LEVEL

- 条件：
  - `lambda.cache.type=MULTI_LEVEL`
  - classpath 同时存在 `Caffeine` 与 `RedisTemplate`
- 输出：
  - `MultiLevelCacheManager`
  - `CacheMessageListener`
  - `RedisMessageListenerContainer`
  - `cacheNodeId`（默认随机 UUID）

## 配置模型

前缀：`lambda.cache`

主要字段：

- `enabled`：总开关
- `type`：`REDIS / CAFFEINE / MULTI_LEVEL`，默认 `REDIS`
- `defaults`：默认缓存配置
- `caches`：按缓存名覆盖配置

`CacheConfigProperties` 关键字段：

- `ttl`：L2 过期时间（默认 `1h`）
- `l1Ttl`：L1 过期时间（多级缓存生效）
- `maxSize` / `initialCapacity`
- `allowNullValues`
- `keyPrefix`
- `enableStats`
- `expireAfterWrite` / `expireAfterAccess` / `refreshAfterWrite`
- `softValues` / `weakValues` / `weakKeys`

## Caffeine 过期优先级

`CaffeineFactory` 的写后过期策略优先级：

1. `expireAfterWrite`
2. `l1Ttl`
3. `ttl`

即：若未显式设置 `expireAfterWrite`，会回退到 `l1Ttl`，再回退到 `ttl`。

## 多级缓存工作机制

### 读流程

1. 先读 L1（Caffeine）
2. L1 未命中再读 L2（Redis）
3. L2 命中后回填 L1

### 写流程

1. 同时写 L2 与 L1
2. 发布同步消息到 `lambda:cache:topic`

### 删流程

1. 同时删 L2 与 L1
2. 发布同步消息到 `lambda:cache:topic`

### 跨节点一致性策略

- 多节点通过 Redis Pub/Sub 接收 `CacheMessage`。
- 收到其他节点消息后，仅处理本地 L1：
  - `PUT` / `EVICT`：本地 `evict(key)`
  - `PUT_ALL` / `EVICT_ALL`：本地逐 key `evict`
  - `CLEAR`：本地 `clear`
- `PUT` 消息不直接写值，而是失效本地 L1，避免消息体传输大对象并保证下次读取从 L2 拿到最新值。

## 示例配置

### Redis 模式（默认）

```yaml
lambda:
  cache:
    type: REDIS
    defaults:
      ttl: 1h
      key-prefix: "lambda:cache:"
      allow-null-values: true
    caches:
      users:
        ttl: 30m
      sessions:
        ttl: 2h
```

### Caffeine 模式

```yaml
lambda:
  cache:
    type: CAFFEINE
    defaults:
      ttl: 30m
      max-size: 10000
      initial-capacity: 100
      enable-stats: true
```

### 多级缓存模式

```yaml
lambda:
  cache:
    type: MULTI_LEVEL
    defaults:
      ttl: 1h
      l1-ttl: 10m
      max-size: 5000
      key-prefix: "lambda:cache:"
    caches:
      products:
        ttl: 2h
        l1-ttl: 5m
```

## 使用方式

本模块输出的是 Spring 标准 `CacheManager`，建议直接使用 Spring Cache 注解：

```java
@Cacheable(cacheNames = "users", key = "#id")
public User findUser(Long id) {
    return repository.findById(id).orElse(null);
}

@CachePut(cacheNames = "users", key = "#user.id")
public User updateUser(User user) {
    return repository.save(user);
}

@CacheEvict(cacheNames = "users", key = "#id")
public void deleteUser(Long id) {
    repository.deleteById(id);
}
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `spring-boot-starter-cache`
- `spring-context-support`
- `spring-boot-starter-data-redis`（optional）
- `caffeine`（optional）
- `lambda-cloud-core`

## 当前实现约束

- 多级缓存同步只保证“L1 最终一致失效”，不是强一致分布式事务。
- `CacheMessageListener` 依赖 `RedisTemplate` 的 value serializer 反序列化消息，需与发布侧保持一致。
- `REDIS` 与 `MULTI_LEVEL` 的 value 序列化策略不同：
  - `REDIS` 模式固定 `RedisSerializer.json()`
  - `MULTI_LEVEL` 模式跟随 `redisTemplate.getValueSerializer()`
- `MULTI_LEVEL` 下 `refreshAfterWrite` 会为 L1 创建从 L2 回源的 CacheLoader；若 L2 不可用会记录告警并返回空值。
