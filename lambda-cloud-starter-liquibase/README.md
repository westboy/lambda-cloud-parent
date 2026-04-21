# lambda-cloud-starter-liquibase

`lambda-cloud-starter-liquibase` 是 Lambda Cloud 的数据库版本管理 starter，提供统一的 Liquibase 执行入口、主变更集聚合规则、以及“主迁移完成后”的可插拔后置执行机制。

## 模块定位

- 将 Liquibase 迁移能力封装为自动装配组件。
- 统一约定变更日志扫描目录、文件过滤和执行顺序。
- 支持通过 `LiquibasePostExecutor` 在主迁移后执行追加 changelog。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ LiquibaseAutoConfiguration.java
└─ LiquibaseProperties.java

src/main/java/com/lambda/cloud/liquibase/
├─ LiquibaseFinishedPublisher.java
├─ LiquibasePostExecutor.java
├─ comparator/DefaultLiquibaseComparator.java
└─ filter/DefaultLiquibaseFilter.java

src/main/resources/
├─ META-INF/db/changelogs/lambda-master.xml
└─ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.LiquibaseAutoConfiguration
```

## 自动装配机制

`LiquibaseAutoConfiguration` 条件与行为：

- `@AutoConfigureAfter(DataSourceAutoConfiguration.class)`
- `@ConditionalOnProperty(prefix = "lambda.liquibase", name = "enabled", matchIfMissing = true)`
- 总开关：`lambda.liquibase.enabled`（缺省视为 `true`）
- 绑定配置类：`LiquibaseProperties`

### 核心 Bean

- `lambdaLiquibase`（`SpringLiquibase`, `@Primary`）
  - 读取 `lambda.liquibase.url/username/password/driver-class-name`
  - 通过 `DataSourceUtils.getInstance(...)` 构造数据源
  - 固定主 changelog：`classpath:META-INF/db/changelogs/lambda-master.xml`
  - 固定 contexts：`lambda_cloud_liquibase`
- `LiquibaseFinishedPublisher`（`@DependsOn("lambdaLiquibase")`）
  - 注入 `List<LiquibasePostExecutor>`
  - 在 Bean 初始化后逐个执行后置迁移

## 配置模型

配置前缀：`lambda.liquibase`

- `enabled`：默认 `true`
- `url`：数据库连接 URL
- `username`：数据库用户名
- `password`：数据库密码
- `driver-class-name`：数据库驱动类名

最小配置示例：

```yaml
lambda:
  liquibase:
    enabled: true
    url: jdbc:mysql://127.0.0.1:3306/lambda
    username: your-user
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 变更集聚合规则

主入口 changelog：

- `META-INF/db/changelogs/lambda-master.xml`

该文件通过 `includeAll` 扫描同目录并指定：

- `filter = com.lambda.cloud.liquibase.filter.DefaultLiquibaseFilter`
- `resourceComparator = com.lambda.cloud.liquibase.comparator.DefaultLiquibaseComparator`

### 文件过滤规则

`DefaultLiquibaseFilter` 仅允许匹配：

- `lambda-\w*-changelog.xml`

即只有 `lambda-xxx-changelog.xml` 形式的文件会进入执行集合。

### 文件排序规则

`DefaultLiquibaseComparator` 顺序如下：

1. 强制优先：`lambda-datasource-changelog.xml`
2. 普通文件：按文件名字典序
3. 末尾执行：`lambda-additional-changelog.xml`

## 后置执行机制

### LiquibasePostExecutor

- 每个实例绑定一个 `changelog` 路径。
- 执行时会：
  - 创建独立 `SpringLiquibase`
  - 复用主流程数据源
  - `setContexts(IdUtil.fastSimpleUUID())`
  - 调用 `afterPropertiesSet()`

### LiquibaseFinishedPublisher

- `@PostConstruct` 时执行全部后置执行器。
- 单个执行器失败只记录错误，不中断其他执行器。
- 无执行器时仅输出日志并跳过。

后置执行器注册示例：

```java
@Bean
public LiquibasePostExecutor dictPostExecutor() {
    return new LiquibasePostExecutor("classpath:META-INF/db/changelogs/lambda-dict-changelog.xml");
}
```

## 执行链路

1. 条件满足后加载 `LiquibaseAutoConfiguration`
2. 校验 `url` 与 `driver-class-name` 非空
3. 创建 `lambdaLiquibase` 并执行 `lambda-master.xml`
4. `LiquibaseFinishedPublisher` 在 `@PostConstruct` 执行后置执行器
5. 所有后置执行器完成后启动继续

## 依赖说明

关键依赖（见 `pom.xml`）：

- `org.liquibase:liquibase-core`
- `com.lambda.cloud:lambda-cloud-starter-datasource`
- `spring-boot-configuration-processor`（optional）

## 当前实现约束

- 数据源来自 `lambda.liquibase.*`，不是复用业务主数据源 Bean。
- `url` 与 `driver-class-name` 缺失会直接抛 `IllegalArgumentException` 阻断启动。
- 主 changelog 路径固定为 `META-INF/db/changelogs/lambda-master.xml`，不支持配置覆盖。
- `DefaultLiquibaseFilter` 正则仅支持 `\w`，文件名中包含 `-` 的模块段不会匹配。
- `DefaultLiquibaseComparator` 的强制排序列表目前仅一个文件名，扩展性有限。
- 后置执行器 context 每次随机 UUID，不适合依赖固定 context 的变更策略。
