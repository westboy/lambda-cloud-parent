---
name: "lambda-cloud-monitoring"
description: "Lambda Cloud 监控与指标配置指南。当用户需要配置Actuator监控、Micrometer指标采集、健康检查或MeterHelper工具时调用。"
---

# Lambda Cloud 监控配置

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-actuator</artifactId>
</dependency>
```

## 基础配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

spring:
  application:
    name: your-service-name

lambda:
  actuator:
    resource:
      location-pattern: classpath*:com/lambda/cloud/**/*.class
```

## 公共指标标签

自动注入 `application` 标签到所有 Micrometer 指标:

```
your_metric_total{application="your-service-name", ...} 1.0
```

## @Counted 计数器注解

```java
@Service
public class OrderService {

    @Counted(value = "order.created", description = "订单创建计数")
    public OrderVO createOrder(OrderCreateDTO dto) {
        // 业务逻辑
        return orderVO;
    }

    @Counted(value = "order.query", description = "订单查询计数", extraTags = {"type", "list"})
    public List<OrderVO> listOrders() {
        // 业务逻辑
        return orderVOList;
    }
}
```

## @Timed 计时器注解

```java
@Service
public class PaymentService {

    @Timed(value = "payment.process", description = "支付处理耗时")
    public PaymentResult processPayment(PaymentDTO dto) {
        // 业务逻辑
        return result;
    }

    @Timed(
        value = "payment.process",
        description = "支付处理耗时",
        histogram = true,
        percentiles = {0.5, 0.95, 0.99}
    )
    public PaymentResult processPaymentWithPercentiles(PaymentDTO dto) {
        // 业务逻辑
        return result;
    }
}
```

## MeterHelper 编程式指标

`MeterHelper` 是 record 类型，通过构造器注入 `MeterRegistry`:

```java
@Service
public class BusinessService {

    @Autowired
    private MeterHelper meterHelper;

    public void processOrder() {
        // Counter 计数器
        Counter counter = meterHelper.counter(
            "order.process.total",
            "订单处理总量",
            Map.of("type", "normal")
        );
        counter.increment();

        // Gauge 仪表盘
        AtomicInteger queueSize = new AtomicInteger(0);
        meterHelper.gauge(
            "order.queue.size",
            "订单队列大小",
            queueSize,
            AtomicInteger::get
        );

        // Timer 计时器
        Timer timer = meterHelper.timer(
            "order.process.latency",
            "订单处理延迟",
            Map.of("type", "normal")
        );
        timer.record(() -> {
            // 耗时操作
        });

        // MultiGauge 多值仪表盘
        meterHelper.multiGauge(
            "order.status.distribution",
            "订单状态分布",
            Map.of("status", "pending")
        );
    }
}
```

### MeterHelper 方法列表

| 方法 | 说明 |
|------|------|
| `counter(name, description)` | 创建计数器 (无标签) |
| `counter(name, description, String... tags)` | 创建计数器 (varargs 标签) |
| `counter(name, description, Map tags)` | 创建计数器 (Map 标签) |
| `gauge(name, description, object, function)` | 创建仪表盘 (对象监控) |
| `gauge(name, description, tags, object, function)` | 创建仪表盘 (对象监控+标签) |
| `gauge(name, description, number)` | 创建仪表盘 (数值监控) |
| `gauge(name, description, tags, number)` | 创建仪表盘 (数值监控+标签) |
| `multiGauge(name, description)` | 创建多值仪表盘 (无标签) |
| `multiGauge(name, description, tags)` | 创建多值仪表盘 (有标签) |
| `timer(name, description, tags)` | 创建计时器 |

## Info 端点扩展

`PathResourceResolver` 扩展 `/actuator/info`, 输出 Jar 包版本信息:

```json
{
  "back": {
    "packages": {
      "lambda-cloud-starter-web": {
        "version": "2026.1.1-SNAPSHOT",
        "modified": "2026-03-18 12:00:00"
      },
      "lambda-cloud-starter-mybatis": {
        "version": "2026.1.1-SNAPSHOT",
        "modified": "2026-03-18 12:00:00"
      }
    }
  }
}
```

## Prometheus 指标导出

确保引入 Micrometer Prometheus 依赖 (已包含在 actuator starter 中):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

访问 `/actuator/prometheus` 获取 Prometheus 格式指标。

## Dubbo 健康检查

如果同时引入 `lambda-cloud-starter-dubbo`, 自动注册 `DubboHealthIndicator`:

- 从 `DubboMetricsCollector` 聚合健康状态
- 单服务判定: 请求数 > 10 且成功率 < 95% 时标记不健康
- 任一服务不健康则整体 DOWN

```json
{
  "status": "UP",
  "components": {
    "dubbo": {
      "status": "UP",
      "details": {
        "user-service": {
          "totalRequests": 100,
          "successRate": 99.0,
          "status": "UP"
        }
      }
    }
  }
}
```

## 自定义健康检查

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 检查业务依赖
        if (isHealthy()) {
            return Health.up()
                .withDetail("message", "服务正常")
                .build();
        } else {
            return Health.down()
                .withDetail("message", "服务异常")
                .build();
        }
    }
}
```

## 最佳实践

1. **指标命名**: 使用小写字母和点分隔, 如 `order.process.total`
2. **标签设计**: 避免高基数标签 (如用户ID), 会导致指标爆炸
3. **百分位数**: 对延迟敏感的接口使用 `percentiles = {0.5, 0.95, 0.99}`
4. **Gauge 监控**: 适合监控队列大小、连接数等可变数值
5. **告警规则**: 基于 Prometheus + AlertManager 配置告警
6. **生产安全**: 生产环境限制 Actuator 端点暴露范围
