# lambda-cloud-starter-mybatis

`lambda-cloud-starter-mybatis` 是 MyBatis-Plus 增强 starter，提供自动装配、扩展 SQL 注入、元字段填充、租户行级隔离、数据权限改写与字段加解密能力。

## 模块定位

- 在 `mybatis-plus-spring-boot4-starter` 基础上补充企业级常用能力。
- 统一输出 MyBatis-Plus 插件链与扩展 Mapper 方法，减少业务重复造轮子。
- 支持和 `lambda-cloud-starter-security` 配合，完成租户与操作人上下文联动。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ MyBatisAutoConfiguration.java
├─ MybatisPlusExtendProperties.java
├─ PurviewAutoConfiguration.java
├─ PurviewProperties.java
└─ condition/MapperPackageConfiguredCondition.java

src/main/java/com/lambda/cloud/mybatis/
├─ annotation/TableCodeField.java
├─ handler/
│  ├─ AesEncryptHandler.java
│  ├─ EntityMetaFiller.java
│  └─ GlobalMetaObjectHandler.java
├─ injector/
│  ├─ LambdaExtendSqlInjector.java
│  └─ method/...（InsertAll / SelectByCode / UpdateByCode / Exists 等）
├─ mapper/LambdaBaseMapper.java
├─ mapping/LambdaBoundSql.java / LambdaSqlSource.java
├─ tenant/
│  ├─ TenantContextHolder.java
│  ├─ TenantExpressionInterceptor.java
│  └─ TenantHandler.java
├─ datascope/
│  ├─ PurviewInterceptor.java
│  ├─ PurviewContext*.java
│  ├─ annotation/DataScope.java
│  ├─ strategy/*.java
│  └─ support/PurviewSqlHelper.java
└─ utils/MybatisUtils.java / SQLUtils.java

src/main/resources/
├─ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
└─ spy.properties
```

自动装配注册项：

```text
com.lambda.autoconfig.MyBatisAutoConfiguration
com.lambda.autoconfig.PurviewAutoConfiguration
```

## 自动装配机制

### 基础装配

`MyBatisAutoConfiguration` 关键行为：

- 引入 `MybatisPlusAutoConfiguration`。
- 注册 `ConfigurationCustomizer`，统一 `JdbcTypeForNull = NULL`。
- 当配置了 `mybatis-plus.mapper-package` 时，动态注册 `MapperScannerConfigurer`。
- 注册 `DatabaseIdProvider`，内置 H2/MySQL/Oracle/PostgreSQL/DB2/DM 映射并支持扩展。
- 注册 `LambdaExtendSqlInjector`，注入扩展 SQL 方法。
- 注册 `GlobalMetaObjectHandler`，聚合执行所有 `EntityMetaFiller`。
- 注册 `JdbcTemplate`（`@Primary`）。
- 注册 `MybatisPlusInterceptor`，按 `Order` 组装所有 `InnerInterceptor`。
- 默认注册分页拦截器 `PaginationInnerInterceptor`（`@Order(20)`）。

### 加密装配

条件：

- `mybatis-plus.encrypt.enabled=true`

输出：

- `AesEncryptHandler`
- `ConfigurationCustomizer`（将 typeHandler 注册到 MyBatis）

### 租户装配

条件：

- `mybatis-plus.tenant.enabled=true`

输出：

- `TenantLineHandler`（默认 `TenantHandler`）
- `TenantLineInnerInterceptor`（`@Order(10)`）
- `TenantExpressionInterceptor`（`@Order(Short.MAX_VALUE)`）

## 配置模型

前缀：`mybatis-plus`

`MybatisPlusExtendProperties` 关键配置：

- `mapper-package`
- `database-id-map`
- `encrypt.enabled` / `encrypt.key`
- `tenant.enabled`
- `tenant.tenant-column`（默认 `tenant_id`）
- `tenant.ignore-tables`（与内置默认忽略表集合做并集）

数据权限配置前缀：`lambda.datascope`

`PurviewProperties` 主要用于配置组织表、权限表、数据视图表字段与管理员用户名白名单。

## 核心能力

### 扩展 Mapper 能力

`LambdaBaseMapper<T>` 在 `BaseMapper<T>` 上增加：

- `insertAll` / `insertAllBatch`
- `selectByCode` / `updateByCode` / `deleteByCode`
- `exists(Wrapper<T>)`

其中 `selectByCode/updateByCode/deleteByCode` 依赖实体字段上的 `@TableCodeField` 标识。

### 批量插入 SQL 注入

`InsertAll` 会按 `databaseId` 选择 SQL 模板：

- Oracle/DM：`INSERT ALL ... SELECT 1 FROM DUAL`
- 其他数据库：`INSERT INTO ... VALUES (...), (...)`

### 元字段自动填充

`GlobalMetaObjectHandler` 会遍历所有 `EntityMetaFiller`，分别执行 `insertFill/updateFill`。
业务只需实现 `EntityMetaFiller` 并注册为 Bean。

### 字段 AES 加解密

`AesEncryptHandler` 在写入时加密、读取时解密：

- 写：`AES.encrypt(value, key)`
- 读：`AES.decrypt(value, key)`

用于 `@TableField(typeHandler = AesEncryptHandler.class)` 的敏感字段。

### 租户行级隔离

租户链路分两步：

1. `TenantExpressionInterceptor`
   - 拦截 MyBatis `query/update`
   - 按优先级提取 tenantId：
     - `ParamMap` 指定字段
     - 参数对象同名属性
     - `OperatorUtils.getSafeOperator().getTenantId()`
   - 写入 `TenantContextHolder`，执行后自动清理
2. `TenantLineInnerInterceptor + TenantHandler`
   - 从 `TenantContextHolder` 读取 tenantId
   - 根据 `tenant.tenant-column` 注入租户条件
   - 命中 `tenant.ignore-tables` 时跳过

### 数据权限 SQL 改写

`PurviewInterceptor` 仅拦截 `SELECT`：

- 从 Mapper 方法读取 `@DataScope`
- 非管理员用户按策略改写 SQL
- 支持三种 `mode`：
  - `SUB_QUERY`
  - `INNER`
  - `STATISTICS`
- 支持 `pretreatment` 预加载权限集合模式
- 若 SQL 含占位标记 `'lambda-permissions|...'`，会进入 replace 模式

管理员判定依据：`lambda.datascope.super-admin-usernames`。

## 配置示例

```yaml
mybatis-plus:
  mapper-package: com.example.**.mapper
  encrypt:
    enabled: true
    key: 1234567890123456
  tenant:
    enabled: true
    tenant-column: tenant_id
    ignore-tables:
      - sys_config
      - sys_dict

lambda:
  datascope:
    super-admin-usernames:
      - admin
```

## 使用示例

```java
public interface UserMapper extends LambdaBaseMapper<UserEntity> {}

@TableName("sys_user")
public class UserEntity {
    @TableId
    private Long id;

    @TableCodeField
    @TableField("user_code")
    private String userCode;

    @TableField(value = "mobile", typeHandler = AesEncryptHandler.class)
    private String mobile;
}
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `mybatis-plus-spring-boot4-starter`
- `mybatis-plus-jsqlparser`
- `p6spy`
- `lambda-cloud-core`
- `lambda-cloud-starter-security`（optional）

## 当前实现约束

- `mybatis-plus.mapper-package` 为空时不会自动注册 `MapperScannerConfigurer`，需业务自行 `@MapperScan`。
- `TenantExpressionInterceptor` 依赖参数名与 `tenant-column` 一致；不一致时会回退到登录用户租户。
- `PurviewInterceptor` 只处理 `SELECT`，不会改写 `UPDATE/DELETE/INSERT`。
- 数据权限策略在复杂 SQL 下依赖 JSqlParser 解析成功；解析失败会导致策略不可用。
- `AesEncryptHandler` 使用配置密钥对称加解密，密钥轮换需业务自行规划迁移策略。
