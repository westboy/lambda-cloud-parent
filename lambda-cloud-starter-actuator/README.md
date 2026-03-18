# lambda-cloud-starter-actuator

`lambda-cloud-starter-actuator` 是 Lambda Cloud 的监控增强 starter，基于 Spring Boot Actuator + Micrometer 提供统一指标切面能力、指标构建工具，以及包版本信息的 `/actuator/info` 扩展。

## 模块定位

- 为应用统一注入 `application` 公共指标标签。
- 开箱支持 `@Counted` / `@Timed` 指标切面。
- 提供 `MeterHelper`，简化 Counter/Gauge/Timer/MultiGauge 编程式接入。
- 通过 `PathResourceResolver` 扩展 info 端点，输出 Jar 包版本与修改时间。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ ActuatorAutoConfiguration.java
└─ ActuatorProperties.java

src/main/java/com/lambda/cloud/actuator/
├─ MeterHelper.java
└─ resolver/
   ├─ PathResourceResolver.java
   └─ ResourceIndicator.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.ActuatorAutoConfiguration
```

## 自动装配机制

`ActuatorAutoConfiguration` 关键行为：

- `@AutoConfigureAfter(MetricsAutoConfiguration, PrometheusMetricsExportAutoConfiguration)`
- 绑定配置：`ActuatorProperties`（前缀 `lambda.actuator`）
- 注入 Bean：
  - `MeterRegistryCustomizer<MeterRegistry>`
  - `CountedAspect`
  - `TimedAspect`
  - `MeterHelper`
  - `PathResourceResolver`

### 公共指标标签

`MeterRegistryCustomizer` 会读取 `spring.application.name` 并统一注入：

- `application={spring.application.name}`

这意味着所有通过 Micrometer 注册的指标默认带应用名标签。

### 切面指标支持

自动注册：

- `CountedAspect`
- `TimedAspect`

因此业务方法可直接使用 Micrometer 注解（如 `@Counted`、`@Timed`）进行埋点。

## 配置模型

配置前缀：`lambda.actuator`

当前生效配置：

- `resource.location-pattern`
  - 默认：`classpath*:com/lambda/cloud/**/*.class`
  - 用于扫描资源并提取 Jar Manifest 信息。

示例：

```yaml
spring:
  application:
    name: lambda-demo

lambda:
  actuator:
    resource:
      location-pattern: classpath*:com/lambda/cloud/**/*.class
```

## MeterHelper 能力

`MeterHelper` 对 `MeterRegistry` 提供常用封装：

- `counter(name, description[, tags])`
- `gauge(name, description, object, function)`
- `gauge(name, description, number)`
- `multiGauge(name, description[, tags])`
- `timer(name, description[, tags])`

支持两种 tags 传参形式：

- `String... tags`
- `Map<String, String>`

示例：

```java
Counter counter = meterHelper.counter("biz.requests.total", "业务请求总量", Map.of("module", "order"));
counter.increment();

Timer timer = meterHelper.timer("biz.request.latency", "业务请求耗时", Map.of("module", "order"));
timer.record(() -> service.process());
```

## Info 端点扩展

`PathResourceResolver` 同时实现 `InfoContributor`，会参与 `/actuator/info` 输出：

- 根据 `locationPattern` 扫描资源
- 识别 Jar URL 并读取 Manifest：
  - `Implementation-Title`
  - `Implementation-Version`
- 记录资源最后修改时间（`Constants.DATE_PATTERN`）
- 以 `packages` 节点写入 info 详情

返回结构示例（简化）：

```json
{
  "back": {
    "packages": {
      "lambda-cloud-starter-web": {
        "version": "2026.1.1-SNAPSHOT",
        "modified": "2026-03-18 12:00:00"
      }
    }
  }
}
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `org.springframework.boot:spring-boot-starter-actuator`
- `io.micrometer:micrometer-registry-prometheus`
- `org.aspectj:aspectjweaver`
- `com.lambda.cloud:lambda-cloud-core`

## 当前实现约束

- `spring.application.name` 通过 `@Value` 强依赖读取，未配置时可能导致启动失败。
- `PathResourceResolver` 扫描范围默认较大，包体复杂时会增加启动阶段资源扫描成本。
- info 输出中顶层 key 固定为 `back`，非标准命名，接入方需要适配。
- `PathResourceResolver` 异常时仅记录日志并返回空结果，不会中断启动。
- `MeterHelper` 只封装基础 Meter 创建，不包含直方图、百分位、长任务计时等高级配置。
