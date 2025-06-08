# Lambda Cloud Redis Starter

基于Spring Boot的Redis增强模块，提供Redis操作工具、延迟队列和键过期监听功能。

## 功能特性

- 增强的Redis操作工具类
- 基于Redisson的延迟队列
- 键过期事件监听
- 支持单机/哨兵/集群模式

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-redis</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 基础配置

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 
    database: 0
    # 模式: STANDALONE(默认)/SENTINEL/CLUSTER
    mode: STANDALONE 
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### 3. Redis操作工具

```java
@Autowired
private RedisHelper redisHelper;

// 设置值
redisHelper.set("key", "value");

// 获取值
String value = redisHelper.get("key");

// 设置过期时间
redisHelper.set("key", "value", 60, TimeUnit.SECONDS);
```

### 4. 延迟队列使用

```yaml
lambda:
  redis:
    delay:
      enabled: true
      queues:
        - name: order-delay-queue
          delay: 30
          timeUnit: SECONDS
          works: 5
```

```java
@Autowired
private RedisDelayedQueueManager delayedQueueManager;

// 添加延迟任务
delayedQueueManager.add("order-delay-queue", taskId, 30, TimeUnit.SECONDS);

// 实现RedisDelayedListener处理任务
@Component
public class OrderDelayListener implements RedisDelayedListener {
    @Override
    public void onMessage(String taskId) {
        // 处理延迟任务
    }
}
```

### 5. 键过期监听

```java
@Component
public class MyKeyExpiredListener implements RedisKeyExpiredListener {
    @Override
    public void onMessage(RedisKeyExpiredEvent<String> event) {
        String expiredKey = new String(event.getSource());
        // 处理键过期事件
    }
}
```

## 注意事项

1. 键过期监听在集群环境下需要特殊处理
2. 延迟队列需要Redisson依赖
3. 默认使用Lettuce作为Redis客户端
4. 配置前缀为"spring.redis"
5. 延迟队列配置前缀为"lambda.redis.delay"
