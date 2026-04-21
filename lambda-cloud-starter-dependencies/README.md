# lambda-cloud-starter-dependencies（BOM）

`lambda-cloud-starter-dependencies` 是 Lambda Cloud 的统一依赖版本管理模块（BOM，Bill of Materials），用于在业务工程中一次性锁定 Lambda Cloud 相关组件及其三方依赖版本，避免依赖冲突。

## 版本与环境

以下版本以父工程 [pom.xml](../pom.xml) 的 properties 为准：

- 版本：`2026.1.1-SNAPSHOT`
- Java：21
- Spring Boot：4.0.2
- Spring Cloud：2025.1.1
- Spring Cloud Alibaba：2025.1.0.0

## 管理的模块

本 BOM 覆盖 `_lambda-cloud-parent` 的全部可发布模块（与父工程 `modules` 对齐）：

- 基础模块：`lambda-cloud-core`、`lambda-cloud-processor`
- Starter 模块：`lambda-cloud-starter-*`

当前仓库包含的 starter（按父 POM 排列）：

- actuator / cache / datasource / dubbo / feign / gateway / iotdb / liquibase / logger / mybatis / nacos / netty / oss / redis / rocketmq / security / sms / sse / swagger / test / web / webclient / websocket / ykc

## 快速开始

### 1）导入 BOM

在业务工程中引入 BOM（推荐使用统一版本变量，便于升级）：

```xml
<properties>
  <lambda-cloud.version>2026.1.1-SNAPSHOT</lambda-cloud.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.lambda.cloud</groupId>
      <artifactId>lambda-cloud-starter-dependencies</artifactId>
      <version>${lambda-cloud.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### 2）按需引入 starter

```xml
<dependencies>
  <dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-security</artifactId>
  </dependency>
  <dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-mybatis</artifactId>
  </dependency>
</dependencies>
```

## 核心版本对照

| 依赖 | 版本 |
| --- | --- |
| Spring Boot | 4.0.2 |
| Spring Cloud | 2025.1.1 |
| Spring Cloud Alibaba | 2025.1.0.0 |
| MyBatis-Plus | 3.5.15 |
| Sa-Token | 1.45.0 |
| Dubbo | 3.3.6 |
| RocketMQ | 2.3.4 |
| IoTDB | 2.0.3 |
| Redisson | 4.3.0 |
| MapStruct | 1.6.3 |

## 发布（可选）

如需将项目发布到 Maven 私服，请在父工程或 CI 环境中配置 `distributionManagement` 与 `settings.xml`（本仓库提供了示例配置文件位置：`assets/maven/settings.xml`），避免在 README 中固化具体私服地址。
