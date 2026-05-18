---
name: "lambda-cloud-datasource"
description: "Lambda Cloud 数据源与数据库管理指南。当用户需要配置数据源、动态切换数据源、执行数据库迁移(Liquibase)或使用IoTDB时序数据库时调用。"
---

# Lambda Cloud 数据源管理

## 模块引入

```xml
<!-- 数据源 -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-datasource</artifactId>
</dependency>

<!-- 数据库迁移 (可选) -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-liquibase</artifactId>
</dependency>

<!-- IoTDB 时序数据库 (可选) -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-iotdb</artifactId>
</dependency>
```

## 单数据源模式

配置 `spring.datasource.url` 时自动启用:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your-user
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
```

**注意**: 单数据源模式内部仍通过 `DynamicRoutingDataSource` 包装，主库键固定为 `primary`。

## 动态多数据源模式

配置 `spring.datasource.dynamic.primary` 时自动启用:

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/master_db
          username: root
          password: root
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave:
          url: jdbc:mysql://localhost:3306/slave_db
          username: readonly
          password: readonly
          driver-class-name: com.mysql.cj.jdbc.Driver
```

### @DS 切换数据源

```java
@Service
public class UserService {

    @DS("master")
    public void createUser(UserEntity user) {
        userMapper.insert(user);
    }

    @DS("slave")
    public List<UserEntity> listUsers() {
        return userMapper.selectList(null);
    }
}
```

## DynamicDataSourceService 运行时管理

提供运行时数据源增删改查与连通性测试:

```java
@Service
public class DataSourceManager {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    // 新增数据源
    public void addDataSource(String id, String url, String username, String password) {
        DataSourceProperty property = new DataSourceProperty();
        property.setId(id);
        property.setUrl(url);
        property.setUsername(username);
        property.setPassword(password);
        property.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dynamicDataSourceService.addDataSource(property);
    }

    // 更新数据源
    public void updateDataSource(String id, DataSourceProperty property) {
        dynamicDataSourceService.updateDataSource(id, property);
    }

    // 删除数据源
    public void removeDataSource(String id) {
        dynamicDataSourceService.removeDataSource(id);
    }

    // 获取数据源
    public DataSource getDataSource(String id) {
        return dynamicDataSourceService.getDataSource(id);
    }

    // 测试连接
    public boolean testConnection(DataSourceProperty property) {
        return dynamicDataSourceService.test(property);
    }
}
```

### DataSourceProperty 字段

| 字段 | 说明 |
|------|------|
| `id` | 数据源标识 |
| `url` | JDBC URL |
| `username` | 用户名 |
| `password` | 密码 |
| `driverClassName` | 驱动类名 |
| `databaseId` | 数据库标识 (测试连接时回填) |
| `schema` | Schema (测试连接时回填) |
| `readOnly` | 是否只读 |

## Liquibase 数据库迁移

### 配置

```yaml
lambda:
  liquibase:
    enabled: true
    url: jdbc:mysql://localhost:3306/your_db
    username: your-user
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 变更集规范

主入口: `META-INF/db/changelogs/lambda-master.xml`

文件命名约定: `lambda-xxx-changelog.xml`

执行顺序:
1. `lambda-datasource-changelog.xml` (强制优先)
2. 其他文件按名字典序
3. `lambda-additional-changelog.xml` (末尾执行)

### 创建变更集

```xml
<!-- lambda-user-changelog.xml -->
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <changeSet id="1" author="developer">
        <createTable tableName="sys_user">
            <column name="id" type="BIGINT">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="username" type="VARCHAR(64)">
                <constraints nullable="false"/>
            </column>
            <column name="create_time" type="DATETIME"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

### 后置执行器

在主迁移完成后执行额外 changelog:

```java
@Bean
public LiquibasePostExecutor dictPostExecutor() {
    return new LiquibasePostExecutor("classpath:META-INF/db/changelogs/lambda-dict-changelog.xml");
}
```

## IoTDB 时序数据库

### Tree 模式配置

```yaml
lambda:
  iotdb:
    tree-dialect: true
    host: localhost
    port: 6667
    user: root
    password: root
    max-size: 10
```

### Table 模式配置

```yaml
lambda:
  iotdb:
    table-dialect: true
    host: localhost
    port: 6667
    user: root
    password: root
    database: test_db
    thrift-max-frame-size: 67108864
```

### 使用示例

```java
@Service
public class IotDbService {
    private final SessionPool sessionPool;

    public IotDbService(SessionPool sessionPool) {
        this.sessionPool = sessionPool;
    }

    public void insert(String deviceId, long timestamp, double value) {
        Session session = null;
        try {
            session = sessionPool.getSession();
            session.insertRecord(deviceId, timestamp,
                Arrays.asList("temperature"),
                Arrays.asList(TSDataType.DOUBLE),
                Arrays.asList(value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                sessionPool.putBack(session);
            }
        }
    }
}
```

### 声明式订阅

```java
@IotDbSubscription(
    consumerId = "consumer1",
    topic = "test_topic",
    consumerGroupId = "group1"
)
public class MyMessageHandler implements MessageHandler {
    @Override
    public void handle(RowRecord record) {
        // 处理订阅消息
        System.out.println("Received: " + record.getFields());
    }
}
```

## 最佳实践

1. **连接池大小**: 根据并发量设置 `maximum-pool-size`，一般 10-20
2. **动态数据源**: 使用 `@DS` 注解切换，避免手动管理
3. **Liquibase**: 每个版本的变更集放在独立文件中，便于管理
4. **IoTDB**: Tree 模式适合设备数据，Table 模式适合分析查询
5. **敏感信息**: 数据库密码使用环境变量或配置中心加密
