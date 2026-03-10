# lambda-cloud-starter-datasource 数据源模块

## 功能概述

本模块提供标准数据源和动态数据源两种配置方式，基于 Spring Boot 自动配置实现。支持多种数据库类型，包括 MySQL、Oracle 和 Trino。

## 支持的数据库

| 数据库 | 驱动 | 说明 |
|--------|------|------|
| MySQL | mysql-connector-j | 默认为 MySQL |
| Oracle | ojdbc8 | 需要 Oracle JDBC 驱动 |
| Trino | trino-jdbc | 支持 Trino 查询引擎 |

## 配置方式

### 1. 标准数据源配置

使用 HikariCP 连接池，配置前缀为 `spring.datasource.hikari`。

示例配置：

```yaml
spring:
  datasource:
    hikari:
      jdbc-url: jdbc:mysql://localhost:3306/db
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
```

### 2. 动态数据源配置

基于 baomidou 的动态数据源实现，需满足 `DynamicDataSourceCondition` 条件才会生效。

多数据源配置示例：

```yaml
spring:
  datasource:
    dynamic:
      primary: master # 默认数据源
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/master
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave:
          url: jdbc:mysql://localhost:3306/slave
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 多数据库类型配置

```yaml
spring:
  datasource:
    dynamic:
      primary: mysql-db
      datasource:
        mysql-db:
          url: jdbc:mysql://localhost:3306/mydb
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver
        oracle-db:
          url: jdbc:oracle:thin:@localhost:1521:orcl
          username: root
          password: 123456
          driver-class-name: oracle.jdbc.OracleDriver
        trino-db:
          url: jdbc:trino://localhost:8080/hive
          username: root
          password: ""
          driver-class-name: io.trino.jdbc.TrinoDriver
```

## 核心类说明

- `StandardDataSourceConfigurer`: 标准数据源自动配置类
- `DynamicDataSourceConfigurer`: 动态数据源自动配置类
- `DynamicDataSourceService`: 动态数据源服务接口
- `DynamicDataSourceServiceImpl`: 动态数据源服务实现
- `DataSourceProperty`: 数据源属性定义类
  - 包含 url、username、password 等基本连接属性
  - 支持设置数据源 ID、数据库类型等扩展属性

## 依赖

该模块包含以下关键依赖：

- `spring-boot-starter-jdbc`: JDBC 支持
- `dynamic-datasource-spring-boot4-starter`: 动态数据源
- `mysql-connector-j`: MySQL 驱动
- `ojdbc8`: Oracle 驱动
- `trino-jdbc`: Trino 驱动
- `liquibase-core`: 数据库版本管理

## 使用注意事项

1. 标准数据源和动态数据源只能启用一种
2. 使用动态数据源需要添加相关依赖
3. 配置属性需严格按照规范设置
4. Oracle 数据库需要额外的 orai18n 依赖
5. Trino 数据库用于大规模数据分析场景
