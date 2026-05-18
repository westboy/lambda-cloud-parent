---
name: "lambda-cloud-mybatis"
description: "Lambda Cloud MyBatis-Plus 增强配置指南。当用户需要配置 ORM、编写 Mapper、使用多租户隔离、数据权限改写、字段加密或扩展 Mapper 方法时调用。"
---

# Lambda Cloud MyBatis-Plus 增强

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-mybatis</artifactId>
</dependency>
```

## 基础配置

```yaml
mybatis-plus:
  mapper-package: com.your.company.**.mapper
  database-id-map:
    mysql: MySQL
    postgresql: PostgreSQL
    oracle: Oracle
  encrypt:
    enabled: true
    key: 1234567890123456   # 16 字节 AES 密钥
  tenant:
    enabled: true
    tenant-column: tenant_id
    ignore-tables:
      - sys_config
      - sys_dict
      - sys_tenant
```

## 扩展 Mapper: LambdaBaseMapper

`LambdaBaseMapper<T>` 在 `BaseMapper<T>` 基础上增加:

| 方法 | 说明 |
|------|------|
| `int insertAll(List<T> entity)` | 批量插入 (按数据库方言自动适配 SQL) |
| `default int insertAllBatch(List<T> entity)` | 分批批量插入，默认批次大小 1000 |
| `default int insertAllBatch(List<T> entity, int max)` | 自定义批次大小的分批批量插入 |
| `T selectByCode(String code)` | 按编码字段查询 (需 @TableCodeField) |
| `int updateByCode(@Param(Constants.ENTITY) T entity)` | 按编码字段更新 |
| `int deleteByCode(String code)` | 按编码字段删除 |
| `boolean exists(Wrapper<T> queryWrapper)` | 判断是否存在 |

常量:

| 常量 | 值 | 说明 |
|------|------|------|
| `BATCH_SIZE` | 1000 | 批量插入默认批次大小 |

### 使用示例

```java
@Mapper
public interface UserMapper extends LambdaBaseMapper<UserEntity> {
}

// 使用扩展方法
List<UserEntity> users = Arrays.asList(user1, user2, user3);
userMapper.insertAll(users);

// 分批插入（默认每批 1000）
userMapper.insertAllBatch(largeList);

// 自定义批次大小
userMapper.insertAllBatch(largeList, 500);

UserEntity user = userMapper.selectByCode("USER001");
boolean exists = userMapper.exists(new LambdaQueryWrapper<UserEntity>()
    .eq(UserEntity::getUsername, "admin"));
```

### @TableCodeField 编码字段

`@TableCodeField` 位于 `com.lambda.cloud.mybatis.injector` 包下，在实体上标识编码字段，用于 selectByCode/updateByCode/deleteByCode:

```java
import com.lambda.cloud.mybatis.injector.TableCodeField;

@Data
@TableName("sys_user")
public class UserEntity extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableCodeField
    private String userCode;

    private String username;
}
```

## 自动填充: EntityMetaFiller

实现 `EntityMetaFiller` 接口并注册为 Bean，自动填充审计字段。

`EntityMetaFiller` 位于 `com.lambda.cloud.mybatis.handler` 包下，方法签名包含 `MetaObjectHandler handler` 参数:

```java
public interface EntityMetaFiller {
    default void insertFill(MetaObjectHandler handler, MetaObject metaObject) {}
    default void updateFill(MetaObjectHandler handler, MetaObject metaObject) {}
}
```

使用示例:

```java
@Component
public class UserMetaFiller implements EntityMetaFiller {
    @Override
    public void insertFill(MetaObjectHandler handler, MetaObject metaObject) {
        handler.strictInsertFill(metaObject, "createUser", String.class, OperatorUtils.getOperatorId());
        handler.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        handler.strictInsertFill(metaObject, "updateUser", String.class, OperatorUtils.getOperatorId());
        handler.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObjectHandler handler, MetaObject metaObject) {
        handler.strictUpdateFill(metaObject, "updateUser", String.class, OperatorUtils.getOperatorId());
        handler.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

实体字段标注:
```java
@TableField(fill = FieldFill.INSERT)
private String createUser;

@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(fill = FieldFill.INSERT_UPDATE)
private String updateUser;

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

## 字段 AES 加密

配置 `mybatis-plus.encrypt.enabled=true` 后，使用 `AesEncryptHandler`。

`AesEncryptHandler` 构造需要传入 `key` 参数:

```java
public class AesEncryptHandler extends BaseTypeHandler<String> {
    public final String key;

    public AesEncryptHandler(String key) {
        this.key = key;
    }
}
```

在实体中使用:

```java
@Data
@TableName("sys_user")
public class UserEntity extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;

    @TableField(typeHandler = AesEncryptHandler.class)
    private String phone;    // 写入时加密，读取时解密

    @TableField(typeHandler = AesEncryptHandler.class)
    private String idCard;   // 敏感字段加密存储
}
```

## 多租户行级隔离

配置 `mybatis-plus.tenant.enabled=true`:

### 工作机制

1. `TenantExpressionInterceptor` 拦截 MyBatis query/update
2. 按优先级提取 tenantId: ParamMap > 参数对象属性 > `OperatorUtils.getSafeOperator().getTenantId()`
3. 写入 `TenantContextHolder`，执行后自动清理
4. `TenantLineInnerInterceptor` 从 `TenantContextHolder` 读取 tenantId
5. 根据 `tenant.tenant-column` 自动注入租户条件
6. 命中 `tenant.ignore-tables` 时跳过

### TenantContextHolder

`TenantContextHolder` 实现 `AutoCloseable`，支持 try-with-resources:

```java
// 设置租户（非空校验）
TenantContextHolder.getInstance().setTenantId("tenant001");

// 获取当前租户
String tenantId = TenantContextHolder.getCurrentTenantId();

// 安全执行带租户上下文的代码块
String result = TenantContextHolder.runWithTenant("tenant001", () -> {
    return someService.query();
});

// 使用 try-with-resources 自动清理
try (TenantContextHolder holder = TenantContextHolder.getInstance()) {
    holder.setTenantId("tenant001");
    // 业务逻辑
}
```

## 数据权限 SQL 改写

### 配置

```yaml
lambda:
  datascope:
    super-admin-identifiers:
      - admin
    organization-table-name: la_organization
    organization-table-alias: org
    organization-id-column: id
    organization-parent-keys-column: parent_keys
    data-scope-table-name: la_datascopes
    data-scope-table-alias: datascope
    data-scope-id-column: id
    data-scope-tid-column: tid
    data-scope-target-type-column: target_type
    data-scope-type-column: domain_type
    data-scope-rank-column: rank_level
    data-scope-checked-column: checked
```

### 使用 @DataScope

`@DataScope` 位于 `com.lambda.cloud.mybatis.datascope.annotation` 包下:

```java
@Service
public class UserService {

    @DataScope(key = "d.id", type = {1, 2})
    public List<UserEntity> listUsers() {
        return userMapper.selectListWithDept();
    }
}
```

### @DataScope 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `key` | String | `"T.id"` | 数据权限关联的字段名 |
| `level` | int | `Integer.MAX_VALUE` | 所匹配的数据权限级别 |
| `levelExp` | Expression | `Expression.EQ` | 级别表达式 |
| `type` | int[] | `{0}` | 权限树类型 |
| `pretreatment` | boolean | `false` | 开启预处理功能 |
| `mode` | Mode | `Mode.SUB_QUERY` | 查询模式 |
| `scheme` | Scheme | `Scheme.CASCADE` | 权限方案 |
| `condition` | String | `""` | 自定义条件 |
| `checked` | int | `0` | 选中状态 (0:忽略, 1:全选, 2:半选) |

### 三种模式 (Mode)

- `SUB_QUERY`: 子查询模式，SQL 中自动追加 `WHERE dept_id IN (SELECT ...)`
- `INNER`: 内连接模式，SQL 中自动追加 `INNER JOIN`
- `STATISTICS`: 统计模式，适用于聚合查询

### 权限方案 (Scheme)

- `CASCADE`: 级联方案
- `ORGANIZATION`: 按照组织授权

### 级别表达式 (Expression)

- `EQ`: 等于 (=)
- `GT`: 大于 (>)
- `LT`: 小于 (<)
- `GE`: 大于等于 (>=)
- `LE`: 小于等于 (<=)

### 权限集合预加载

```java
@DataScope(key = "T.id", pretreatment = true)
public List<UserEntity> listUsers() {
    // 权限集合已预加载到 DataScopeContextHolder
    return userMapper.selectListWithDept();
}
```

### DataScopeEvaluator 工具类

`DataScopeEvaluator` 位于 `com.lambda.cloud.mybatis.datascope` 包下，提供数据权限评估的静态方法:

```java
public final class DataScopeEvaluator {
    // 判断当前用户是否是数据的拥有者
    static boolean isOwner(LoginUser operator);

    // 获取数据权限ID集合
    static Set<String> getDataScopeIds(LoginUser operator);

    // 获取数据权限注解级别
    static int getLevel(DataScopeContext context);

    // 构建带有高级策略过滤的SQL
    static String buildStrategyScopeSql(DataScopeContext context, LoginUser operator);
}
```

## 自定义 SQL 注入

继承 `LambdaSqlInjector` 添加自定义 SQL 方法:

```java
@Component
public class CustomSqlInjector extends LambdaSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methods = super.getMethodList(configuration, mapperClass, tableInfo);
        methods.add(new CustomMethod());
        return methods;
    }
}
```

## 分页配置

分页拦截器已自动注册 (`@Order(20)`)，使用方式:

```java
// 使用 BasePageDTO
Page<UserEntity> page = queryDTO.getPage();
Page<UserEntity> result = userMapper.selectPage(page, wrapper);

// 手动创建分页
Page<UserEntity> page = new Page<>(1, 20);
Page<UserEntity> result = userMapper.selectPage(page, null);
```

## 常见问题

1. **Mapper 扫描不到**: 确保 `mybatis-plus.mapper-package` 配置正确
2. **租户条件未注入**: 检查 `mybatis-plus.tenant.enabled=true` 和 TenantContextHolder 中的值
3. **加密字段查询**: AES 加密字段不能直接用 WHERE 条件查询，需要先加密条件值
4. **批量插入失败**: 检查数据库方言是否支持，Oracle 使用 `INSERT ALL` 语法
5. **数据权限不生效**: 确认用户不在 `super-admin-identifiers` 白名单中
