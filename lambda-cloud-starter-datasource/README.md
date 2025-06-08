# lambda-cloud-starter-datasource 数据源模块

## 功能概述
本模块提供标准数据源和动态数据源两种配置方式，基于Spring Boot自动配置实现。

## 配置方式

### 1. 标准数据源配置
使用HikariCP连接池，配置前缀为`spring.datasource.hikari`。

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
基于baomidou的动态数据源实现，需满足`DynamicDataSourceCondition`条件才会生效。

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

## 核心类说明

- `StandardDataSourceConfigurer`: 标准数据源自动配置类
- `DynamicDataSourceConfigurer`: 动态数据源自动配置类
- `DataSourceProperty`: 数据源属性定义类
  - 包含url、username、password等基本连接属性
  - 支持设置数据源ID、数据库类型等扩展属性

## 使用注意事项
1. 标准数据源和动态数据源只能启用一种
2. 使用动态数据源需要添加相关依赖
3. 配置属性需严格按照规范设置
