# lambda-cloud-starter-xxljob

`lambda-cloud-starter-xxljob` 基于 `xxl-job-core`（com.xuxueli）提供 XXL-Job **执行器**的自动装配，核心目标是统一 ChargeMind 各模块的分布式定时任务接入，落实工程契约 §23（多实例唯一执行、调度参数集中在调度中心、禁止零散引入 `xxl-job-core`）。

## 模块定位

- 自动装配 `XxlJobSpringExecutor`，扫描并注册本服务内 `@XxlJob` 注解的 handler。
- 仅承载**执行器连接参数**；调度参数（cron、路由策略、失败重试、超时）一律在 XXL-Job 调度中心配置，禁止经属性硬编码（契约 §23.3.2）。
- 本期仅接入**执行器**；`xxl-job-admin` 调度中心由运维独立部署。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ XxlJobProperties.java
└─ XxlJobAutoConfiguration.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.XxlJobAutoConfiguration
```

## 自动装配机制

[XxlJobAutoConfiguration](src/main/java/com/lambda/autoconfig/XxlJobAutoConfiguration.java) 默认关闭，仅当 `lambda.cloud.xxl-job.enabled=true` 时注册：

- `XxlJobSpringExecutor` -> 按 `XxlJobProperties` 装配执行器连接

默认关闭是为避免无调度中心的环境启动报错；部署环境连上调度中心后由环境配置开启。

## 快速开始

### 1）引入依赖

```xml
<dependency>
  <groupId>com.lambda.cloud</groupId>
  <artifactId>lambda-cloud-starter-xxljob</artifactId>
</dependency>
```

版本由 `lambda-cloud-starter-dependencies` 统一管理，禁止模块自填版本（契约 §23.5）。

### 2）最小配置（执行器连接）

```yaml
lambda:
  cloud:
    xxl-job:
      enabled: true
      admin-addresses: http://127.0.0.1:8080/xxl-job-admin
      access-token:
      appname: chargemind-trade
      # address / ip 留空自动发现；port 默认 9999（同机多执行器须错开）
```

### 3）声明一个 JobHandler

```java
@Component
public class SessionReconcileTask {

    @XxlJob("trade.sessionReconcile")
    public void execute() {
        // 业务逻辑：按行 try-catch 隔离 + 幂等
    }
}
```

`@XxlJob` 的 value 为 handler 名，全局唯一，命名约定 `{domain}.{action}`（契约 §23.3.1）。

## 核心组件

- `XxlJobProperties`：`lambda.cloud.xxl-job.*` 执行器连接参数（enabled/adminAddresses/accessToken/appname/address/ip/port/logPath/logRetentionDays）。
- `XxlJobAutoConfiguration`：条件装配 `XxlJobSpringExecutor`。

## 当前实现约束

- 只装配执行器，不内置任何业务 handler；handler 由各业务模块声明 `@XxlJob` 提供。
- cron、路由、重试、超时不在属性中，必须在调度中心配置——属性里刻意不提供这些字段。
- 多实例下由调度中心按路由策略保证唯一执行，模块内不得再叠加 `@Scheduled` + Redis 锁的过渡方案（契约 §23.5）。
