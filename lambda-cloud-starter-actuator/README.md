# Lambda Cloud Actuator Starter

基于Spring Boot Actuator的监控增强模块，提供指标收集和资源解析功能。

## 核心功能

### 1. 指标监控
- **MeterHelper**: 指标收集辅助工具类
```java
// 记录方法调用指标
MeterHelper.recordMethodInvocation("serviceName", "methodName", duration);

// 记录自定义指标
MeterHelper.recordCustomMetric("metric.name", value, tags);
```

### 2. 资源解析
- **PathResourceResolver**: 解析请求路径资源
```java
String resource = PathResourceResolver.resolve(requestPath);
```
- **ResourceIndicator**: 资源指标标记接口
```java
public class MyResource implements ResourceIndicator {
    public String getResourceName() {
        return "my-resource";
    }
}
```

## 配置说明

### 启用功能
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,resource-stats
```

### 自定义指标配置
```yaml
lambda:
  actuator:
    metrics:
      enabled: true
      resource-metrics: true
```

## 依赖
- Spring Boot Actuator
- Micrometer Core

## 注意事项
1. 需要配合Spring Boot Actuator使用
2. 资源解析功能需实现ResourceIndicator接口
3. 指标名称需符合Micrometer命名规范
