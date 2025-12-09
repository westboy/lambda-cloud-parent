# Lambda Cloud Starter Cache

Lambda Cloud 统一缓存抽象层,支持多种缓存实现。

## 功能特性

- 🚀 **统一接口**: 提供统一的缓存操作接口,支持多种缓存实现
- 🔄 **多级缓存**: 支持 L1(Caffeine 本地缓存) + L2(Redis 分布式缓存)的多级缓存架构
- 📊 **统计信息**: 内置缓存命中率、加载时间等统计信息
- ⚙️ **灵活配置**: 支持全局配置和单个缓存的细粒度配置
- 🔌 **自动配置**: 基于 Spring Boot Auto-Configuration,开箱即用

## 支持的缓存类型

| 缓存类型 | 说明 | 适用场景 |
|---------|------|---------|
| **REDIS** | Redis 分布式缓存 | 分布式系统,需要共享缓存数据 |
| **CAFFEINE** | Caffeine 本地缓存 | 单机应用,高性能本地缓存需求 |
| **MULTI_LEVEL** | 多级缓存 | 结合本地缓存和分布式缓存的优势 |

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-cache</artifactId>
</dependency>
```

### Redis 缓存配置

```yaml
lambda:
  cache:
    enabled: true
    type: REDIS  # 使用 Redis 缓存
    defaults:
      ttl: 1h    # 默认过期时间
      enable-stats: true
      key-prefix: "lambda:cache:"
```

### Caffeine 本地缓存配置

```yaml
lambda:
  cache:
    enabled: true
    type: CAFFEINE  # 使用 Caffeine 本地缓存
    defaults:
      ttl: 30m
      max-size: 10000
      initial-capacity: 100
      enable-stats: true
```

### 多级缓存配置

```yaml
lambda:
  cache:
    enabled: true
    type: MULTI_LEVEL  # 使用多级缓存
    defaults:
      ttl: 1h
      max-size: 5000
      enable-stats: true
```

## 使用示例

### 基本使用

```java
@Service
public class UserService {

    @Autowired
    private CacheManager cacheManager;

    public User getUserById(Long userId) {
        Cache<Long, User> cache = cacheManager.getOrCreateCache("users");

        // 从缓存获取,如果不存在则加载
        return cache.get(userId, id -> {
            // 从数据库加载
            return userRepository.findById(id).orElse(null);
        });
    }

    public void updateUser(User user) {
        userRepository.save(user);

        // 更新缓存
        Cache<Long, User> cache = cacheManager.getOrCreateCache("users");
        cache.put(user.getId(), user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);

        // 清除缓存
        Cache<Long, User> cache = cacheManager.getOrCreateCache("users");
        cache.evict(userId);
    }
}
```

### 批量操作

```java
// 批量获取
Set<Long> userIds = Set.of(1L, 2L, 3L);
Map<Long, User> users = cache.getAll(userIds);

// 批量设置
Map<Long, User> userMap = new HashMap<>();
userMap.put(1L, user1);
userMap.put(2L, user2);
cache.putAll(userMap);

// 批量删除
Set<Long> idsToDelete = Set.of(1L, 2L);
cache.evictAll(idsToDelete);
```

### 带过期时间的缓存

```java
// 设置缓存,1小时后过期
cache.put("token:" + userId, token, Duration.ofHours(1));

// 设置缓存,如果不存在
boolean success = cache.putIfAbsent("lock:" + resourceId, "locked", Duration.ofMinutes(5));
```

### 查看缓存统计

```java
CacheStats stats = cache.getStats();
System.out.println("命中率: " + stats.hitRate());
System.out.println("命中次数: " + stats.getHitCount());
System.out.println("未命中次数: " + stats.getMissCount());
System.out.println("缓存大小: " + stats.getSize());
System.out.println("平均加载时间: " + stats.averageLoadPenalty() + "ms");
```

## 配置说明

### 全局配置

```yaml
lambda:
  cache:
    enabled: true              # 是否启用缓存
    type: REDIS               # 缓存类型: REDIS, CAFFEINE, MULTI_LEVEL
    defaults:                 # 默认配置
      ttl: 1h                # 默认过期时间
      max-size: 10000        # 最大缓存条目数(仅本地缓存)
      initial-capacity: 100  # 初始容量(仅本地缓存)
      allow-null-values: true
      key-prefix: "lambda:cache:"
      enable-stats: true
      expire-after-write: 1h      # 写入后过期(仅本地缓存)
      expire-after-access: 30m    # 访问后过期(仅本地缓存)
      refresh-after-write: 10m    # 刷新时间(仅本地缓存)
      soft-values: false          # 软引用值(仅本地缓存)
      weak-values: false          # 弱引用值(仅本地缓存)
      weak-keys: false            # 弱引用键(仅本地缓存)
```

### 单个缓存配置

```yaml
lambda:
  cache:
    type: REDIS
    defaults:
      ttl: 1h
    caches:
      users:               # 用户缓存
        ttl: 30m
        key-prefix: "user:"
      sessions:            # 会话缓存
        ttl: 2h
        key-prefix: "session:"
      temporary:           # 临时缓存
        ttl: 5m
        key-prefix: "temp:"
```

### 使用单个缓存配置

```java
// 获取使用特定配置的缓存
Cache<Long, User> userCache = cacheManager.getOrCreateCache("users");

// 或者手动指定配置
CacheConfig config = CacheConfig.builder()
    .cacheName("products")
    .ttl(Duration.ofMinutes(30))
    .keyPrefix("product:")
    .build();
Cache<Long, Product> productCache = cacheManager.getOrCreateCache("products", config);
```

## 多级缓存原理

多级缓存使用 L1(Caffeine) + L2(Redis) 的架构:

1. **读取流程**:
   - 先从 L1 本地缓存读取(快速)
   - L1 未命中,从 L2 Redis 读取
   - L2 命中后同步到 L1

2. **写入流程**:
   - 同时写入 L1 和 L2
   - 保证数据一致性

3. **删除流程**:
   - 同时从 L1 和 L2 删除

```java
// 多级缓存使用示例
@Service
public class ProductService {

    @Autowired
    private CacheManager cacheManager; // 配置为 MULTI_LEVEL

    public Product getProduct(Long productId) {
        Cache<Long, Product> cache = cacheManager.getOrCreateCache("products");

        return cache.get(productId, id -> {
            // 从数据库加载
            return productRepository.findById(id).orElse(null);
        });
        // 第一次: L1 miss -> L2 miss -> DB load -> save to L1 & L2
        // 第二次: L1 hit (极快)
        // 后续: 如果 L1 过期但 L2 未过期, L2 hit -> sync to L1
    }
}
```

## API 参考

### Cache 接口

| 方法 | 说明 |
|------|------|
| `get(K key)` | 获取缓存值 |
| `get(K key, Callable<V> loader)` | 获取缓存值,不存在则加载 |
| `get(K key, Function<K, V> loader)` | 获取缓存值,不存在则加载 |
| `getAll(Set<K> keys)` | 批量获取 |
| `put(K key, V value)` | 设置缓存 |
| `put(K key, V value, Duration duration)` | 设置缓存(带过期时间) |
| `putIfAbsent(K key, V value)` | 如果不存在则设置 |
| `putAll(Map<K, V> map)` | 批量设置 |
| `evict(K key)` | 删除缓存 |
| `evictAll(Set<K> keys)` | 批量删除 |
| `clear()` | 清空缓存 |
| `exists(K key)` | 检查键是否存在 |
| `size()` | 获取缓存大小 |
| `expire(K key, Duration duration)` | 设置过期时间 |
| `getExpire(K key)` | 获取剩余过期时间 |
| `getStats()` | 获取统计信息 |

### CacheManager 接口

| 方法 | 说明 |
|------|------|
| `getCache(String name)` | 获取缓存 |
| `getOrCreateCache(String name)` | 获取或创建缓存 |
| `getOrCreateCache(String name, CacheConfig config)` | 获取或创建缓存(指定配置) |
| `getCacheNames()` | 获取所有缓存名称 |
| `destroyCache(String name)` | 销毁缓存 |
| `destroyAll()` | 销毁所有缓存 |
| `getCacheType()` | 获取缓存类型 |

## 性能优化建议

1. **选择合适的缓存类型**:
   - 单机应用使用 CAFFEINE
   - 分布式系统使用 REDIS
   - 高并发读多写少场景使用 MULTI_LEVEL

2. **合理设置过期时间**:
   - 根据数据更新频率设��� TTL
   - 热点数据使用较长的过期时间
   - 临时数据使用较短的过期时间

3. **使用批量操作**:
   - 批量获取: `getAll()`
   - 批量设置: `putAll()`
   - 批量删除: `evictAll()`

4. **监控缓存统计**:
   - 定期检查缓存命中率
   - 根据统计信息调整缓存策略

## 注意事项

1. **Caffeine 限制**:
   - 不支持单个键的 TTL,只能使用全局 TTL
   - 不支持分布式环境的数据共享

2. **Redis 依赖**:
   - 使用 REDIS 或 MULTI_LEVEL 类型需要配置 Redis 连接
   - 需要引入 `lambda-cloud-starter-redis` 依赖

3. **键前缀**:
   - 建议为每个缓存设置唯一的键前缀,避免键冲突
   - 多级缓存会自动为 L1 和 L2 添加后缀

4. **空值处理**:
   - 默认允许缓存空值,可通过 `allow-null-values` 配置
   - 注意空值与键不存在的区别

## 依赖

- Spring Boot Starter Cache
- Spring Data Redis (可选,REDIS 和 MULTI_LEVEL 类型需要)
- Caffeine (可选,CAFFEINE 和 MULTI_LEVEL 类型需要)
- Lambda Cloud Core