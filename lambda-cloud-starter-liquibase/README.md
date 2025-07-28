# Lambda Cloud Starter Liquibase

## 模块概述

`lambda-cloud-starter-liquibase` 是基于 Liquibase 的数据库版本管理 Spring Boot Starter，提供了自动化的数据库变更管理功能。该模块支持多变更日志文件的有序执行、后置处理器扩展以及灵活的配置管理。

## 功能特性

### 1. 自动配置
- 基于 Spring Boot 自动配置机制
- 支持条件化启用/禁用
- 自动创建和管理数据源

### 2. 变更日志管理
- 支持多个变更日志文件的有序执行
- 内置文件过滤器，自动识别符合命名规范的变更日志
- 智能文件排序，确保变更按正确顺序执行

### 3. 后置处理器
- 支持在主变更日志执行完成后运行额外的变更脚本
- 灵活的扩展机制，支持自定义后置处理逻辑
- 异常隔离，单个后置处理器失败不影响其他处理器

### 4. 安全性和稳定性
- 完善的参数验证和异常处理
- 详细的日志记录，便于问题排查
- 线程安全的实现

## 核心组件

### LiquibaseAutoConfiguration
自动配置类，负责：
- 创建和配置 SpringLiquibase Bean
- 管理数据源连接
- 注册后置处理器发布器

### LiquibaseProperties
配置属性类，支持以下配置项：
- `enabled`: 是否启用 Liquibase（默认：true）
- `url`: 数据库连接 URL
- `username`: 数据库用户名
- `password`: 数据库密码
- `driverClassName`: 数据库驱动类名

### LiquibasePostExecutor
后置执行器，用于执行额外的变更脚本：
- 支持指定变更日志文件路径
- 自动生成唯一的执行上下文
- 完善的异常处理和日志记录

### DefaultLiquibaseFilter
变更日志文件过滤器：
- 匹配模式：`lambda-{模块名}-changelog.xml`
- 自动排除不符合命名规范的文件
- 性能优化的正则表达式匹配

### DefaultLiquibaseComparator
变更日志文件比较器：
- 支持强制优先级排序
- 特殊文件（如附加变更日志）的特殊处理
- 默认按文件名字典序排序

## 配置说明

### 基本配置
```yaml
lambda:
  liquibase:
    enabled: true
    url: jdbc:mysql://localhost:3306/test
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 配置项详解

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `lambda.liquibase.enabled` | boolean | true | 是否启用 Liquibase |
| `lambda.liquibase.url` | String | - | 数据库连接 URL（必填） |
| `lambda.liquibase.username` | String | - | 数据库用户名 |
| `lambda.liquibase.password` | String | - | 数据库密码 |
| `lambda.liquibase.driver-class-name` | String | - | 数据库驱动类名（必填） |

## 使用示例

### 1. 基本使用

#### 添加依赖
```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-liquibase</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 配置数据库连接
```yaml
lambda:
  liquibase:
    enabled: true
    url: jdbc:mysql://localhost:3306/mydb
    username: myuser
    password: mypassword
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 2. 创建变更日志文件

在 `src/main/resources/META-INF/db/changelogs/` 目录下创建变更日志文件：

#### lambda-user-changelog.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog 
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <changeSet id="create-user-table" author="developer">
        <createTable tableName="user">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="username" type="VARCHAR(50)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="email" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

</databaseChangeLog>
```

### 3. 自定义后置处理器

```java
@Component
public class CustomLiquibasePostProcessor {
    
    @Bean
    public LiquibasePostExecutor customPostExecutor() {
        return new LiquibasePostExecutor("classpath:db/post-scripts/lambda-custom-changelog.xml");
    }
}
```

### 4. 条件化配置

```java
@Configuration
@ConditionalOnProperty(prefix = "lambda.liquibase", name = "enabled", havingValue = "true")
public class CustomLiquibaseConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public LiquibasePostExecutor defaultPostExecutor() {
        return new LiquibasePostExecutor("classpath:db/lambda-default-post-changelog.xml");
    }
}
```

## 文件命名规范

### 变更日志文件
- **命名格式**: `lambda-{模块名}-changelog.xml`
- **示例**: 
  - `lambda-user-changelog.xml`
  - `lambda-order-changelog.xml`
  - `lambda-product-changelog.xml`

### 特殊文件
- **数据源变更日志**: `lambda-datasource-changelog.xml`（优先执行）
- **附加变更日志**: `lambda-additional-changelog.xml`（最后执行）

## 执行顺序

1. **强制优先级文件**：`lambda-datasource-changelog.xml`
2. **普通变更日志**：按文件名字典序排序
3. **附加变更日志**：`lambda-additional-changelog.xml`
4. **后置处理器**：按注册顺序执行

## 核心依赖

- **Spring Boot**: 自动配置和依赖注入
- **Liquibase Core**: 数据库版本管理核心功能
- **Lambda Cloud Starter Datasource**: 数据源管理
- **Hutool**: 工具类库
- **Lombok**: 代码简化

## 注意事项

1. **数据库连接配置**：
   - `url` 和 `driverClassName` 为必填项
   - 确保数据库驱动已添加到项目依赖中

2. **变更日志文件**：
   - 必须放置在 `META-INF/db/changelogs/` 目录下
   - 文件名必须符合命名规范才会被自动识别
   - 建议使用有意义的 changeSet ID 和 author

3. **后置处理器**：
   - 后置处理器异常不会中断其他处理器的执行
   - 建议在后置处理器中添加适当的异常处理

4. **性能考虑**：
   - 大量变更建议分批执行
   - 避免在生产环境执行耗时较长的变更

5. **安全性**：
   - 生产环境建议使用专门的数据库用户
   - 敏感信息（如密码）建议使用环境变量或配置中心
