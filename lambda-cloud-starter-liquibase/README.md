# lambda-cloud-starter-liquibase 数据库变更管理模块

## 功能概述
本模块基于Liquibase实现数据库变更管理，主要功能包括：
1. 自动执行数据库变更脚本
2. 支持多数据源变更管理
3. 提供变更执行完成后的回调机制

## 核心组件
### 自动配置类
- LiquibaseAutoConfiguration: 配置SpringLiquibase并设置默认变更日志路径
- LiquibaseProperties: 提供Liquibase相关配置属性

### 执行器组件
- LiquibasePostExecutor: 负责执行单个变更脚本
- LiquibaseFinishedPublisher: 在应用启动后执行所有注册的变更脚本

## 配置方式
```properties
# 启用/禁用Liquibase功能
lambda.liquibase.enabled=true

# 数据库连接配置
lambda.liquibase.url=jdbc:mysql://localhost:3306/db
lambda.liquibase.username=root
lambda.liquibase.password=
lambda.liquibase.driver-class-name=com.mysql.cj.jdbc.Driver
```

## 变更日志位置
默认从以下路径加载主变更日志文件：
```
classpath:META-INF/db/changelogs/lambda-master.xml
```

## 实现说明
1. 变更脚本会在应用启动后自动执行
2. 每个变更执行使用唯一上下文ID
3. 支持通过配置属性自定义数据库连接参数
4. 变更日志路径固定，不可配置
