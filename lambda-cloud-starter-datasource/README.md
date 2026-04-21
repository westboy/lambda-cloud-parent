# lambda-cloud-starter-datasource

`lambda-cloud-starter-datasource` 提供统一的数据源自动装配能力，支持：

- 单数据源（标准 `spring.datasource.url`）
- 动态多数据源（`spring.datasource.dynamic.*`）
- 运行时数据源增删改查与连通性测试（`DynamicDataSourceService`）

## 模块定位

- 这是一个基础 starter，负责数据源装配与动态路由能力，不承载业务 DAO 逻辑。
- 对外统一暴露 `DynamicRoutingDataSource` 与 `DynamicDataSourceService`，供上层模块在运行时管理数据源。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
└─ DataSourceAutoConfiguration.java

src/main/java/com/lambda/cloud/datasource/
├─ condition/
│  ├─ DynamicDataSourceCondition.java
│  └─ StandardDataSourceCondition.java
├─ config/
│  ├─ DynamicDataSourceConfigurer.java
│  └─ StandardDataSourceConfigurer.java
├─ dynamic/
│  ├─ DynamicDataSourceService.java
│  └─ impl/DynamicDataSourceServiceImpl.java
├─ property/DataSourceProperty.java
└─ utils/DataSourceUtils.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.DataSourceAutoConfiguration
```

## 自动装配机制

### 装配入口

- `DataSourceAutoConfiguration`
  - `@AutoConfigureBefore(DataSourceAutoConfiguration.class)`：优先于 Spring 默认 JDBC 自动配置。
  - `@Import(StandardDataSourceConfigurer, DynamicDataSourceConfigurer)`：同时引入两套配置，由条件类互斥生效。
  - 注册 `DynamicDataSourceService` Bean（实现为 `DynamicDataSourceServiceImpl`）。

### 条件切换规则

- 动态数据源条件 `DynamicDataSourceCondition`
  - 条件：`spring.datasource.dynamic.primary` 有值。
- 标准数据源条件 `StandardDataSourceCondition`
  - 条件：`spring.datasource.url` 有值，且 `spring.datasource.dynamic.primary` 为空。

两者在常规配置下互斥。

## 核心组件

### StandardDataSourceConfigurer（单数据源）

- 导入 `org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration`。
- 按 `spring.datasource.hikari` 绑定并创建 `HikariDataSource`。
- 将单数据源包装进 `DynamicRoutingDataSource`：
  - 主库键固定为 `primary`
  - 路由策略 `LoadBalanceDynamicDataSourceStrategy`
  - `strict=false`

这意味着即使是单库模式，应用侧仍通过动态路由数据源访问。

### DynamicDataSourceConfigurer（多数据源）

- 直接导入 `com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration`。
- 动态数据源配置由 baomidou starter 接管。

### DynamicDataSourceService

对外运行时管理接口：

- `addDataSource(DataSourceProperty property)`
- `updateDataSource(String id, DataSourceProperty property)`
- `getDataSource(String id)`
- `removeDataSource(String id)`
- `test(DataSourceProperty property)`

实现类 `DynamicDataSourceServiceImpl` 的关键行为：

- 新增时先构建 Hikari，再做连接有效性检测，通过后才注册到 `DynamicRoutingDataSource`。
- 更新时执行“先移除后新增”。
- 删除时直接从路由容器移除。

### DataSourceProperty

核心字段：

- `id`
- `url`
- `username`
- `password`
- `driverClassName`
- `databaseId`
- `schema`
- `readOnly`

说明：

- `setJdbcUrl` 最终写入 `url`，兼容 JDBC 常用字段命名。
- `databaseId/schema` 由测试连接过程回填。

### DataSourceUtils

提供三类能力：

- `getInstance(...)`：按参数快速构造 Hikari 数据源。
- `test(DataSourceProperty)`：
  - 创建临时连接池（最小配置）
  - 验证连接
  - 成功后回填 `schema/databaseId`
- `test(DataSource)`：基于现有数据源执行 `Connection#isValid`。

## 配置示例

### 单数据源模式

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app
    username: your-user
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
```

### 动态多数据源模式

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/master
          username: your-user
          password: your-password
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave:
          url: jdbc:mysql://localhost:3306/slave
          username: your-user
          password: your-password
          driver-class-name: com.mysql.cj.jdbc.Driver
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `spring-boot-starter-jdbc`
- `dynamic-datasource-spring-boot4-starter`
- `liquibase-core`
- `mysql-connector-j`
- `ojdbc8` + `orai18n`
- `trino-jdbc`

## 当前实现约束

- 条件类只检查关键配置项是否存在，不校验配置完整性。
- `DynamicDataSourceServiceImpl#addDataSource` 仅设置基础连接信息，未开放池参数细粒度配置。
- 标准模式主数据源 key 固定为 `primary`。
