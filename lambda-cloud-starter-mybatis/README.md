# Lambda Cloud Starter MyBatis

## 概述

`lambda-cloud-starter-mybatis` 是 Lambda Cloud 微服务框架的 MyBatis-Plus 扩展模块，基于 MyBatis-Plus 提供企业级开发常用功能增强，包括自动配置、自定义 SQL 注入器、字段自动填充、数据权限控制、多租户支持和字段加密等功能。

## 功能特性

- **🔧 自动配置**：自动配置 MyBatis-Plus 核心组件和拦截器
- **📝 扩展 Mapper**：提供 `LambdaBaseMapper` 扩展基础 CRUD 操作
- **🔄 自动填充**：通过 `GlobalMetaObjectHandler` 实现字段自动填充
- **🛡️ 数据权限**：通过 `@Purview` 注解实现数据权限控制
- **🏢 多租户支持**：提供租户数据隔离功能
- **🔐 字段加密**：通过 `AesEncryptHandler` 实现字段级 AES 加密
- **📊 SQL 监控**：集成 P6Spy 进行 SQL 性能分析

## 核心组件

### 1. 自动配置 - MyBatisAutoConfiguration

自动配置类提供以下功能：

- **Mapper 扫描**：默认扫描 `com.lambda.cloud.**.mapper` 包
- **JdbcType 配置**：解决 Oracle 批量插入 NULL 值转换问题
- **数据库类型识别**：支持 H2、MySQL、Oracle、PostgreSQL、DB2、DM 等数据库
- **分页插件**：自动配置分页拦截器
- **批量插入拦截器**：优化批量插入性能
- **JdbcTemplate**：提供 JdbcTemplate Bean

### 2. 扩展 Mapper - LambdaBaseMapper

`LambdaBaseMapper<T>` 扩展了 MyBatis-Plus 的 `BaseMapper`，提供以下额外方法：

#### 批量插入方法
```java
// 动态判断数据库类型的批量插入
int insertAll(List<T> entity);

// 分批批量插入（默认 1000 条一批）
int insertAllBatch(List<T> entity);
int insertAllBatch(List<T> entity, int max);

// MySQL 专用批量插入
int mysqlInsertAllBatch(List<T> entity);

// Oracle 专用批量插入
int oracleInsertAllBatch(List<T> entity);
```

#### 基于编码字段的操作
```java
// 根据编码查询（需要 @TableCodeField 注解标识字段）
T selectByCode(String code);

// 根据编码更新
int updateByCode(T entity);

// 根据编码删除
int deleteByCode(String code);
```

#### 存在性检查
```java
// 判断是否存在记录
boolean exists(Wrapper<T> queryWrapper);
```

### 3. 编码字段注解 - @TableCodeField

用于标识实体类中的唯一编码字段：

```java
public class User {
    @TableId
    private Long id;
    
    @TableCodeField
    @TableField("user_code")
    private String userCode;
    
    // 其他字段...
}
```

### 4. 自动填充 - GlobalMetaObjectHandler

自动填充功能通过 `GlobalMetaObjectHandler` 实现，支持自定义 `EntityMetaFiller`：

```java
@Component
public class CustomEntityMetaFiller implements EntityMetaFiller {
    @Override
    public void insertFill(MetaObjectHandler handler, MetaObject metaObject) {
        // 插入时的自动填充逻辑
        handler.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        handler.strictInsertFill(metaObject, "createUser", String.class, getCurrentUser());
    }
    
    @Override
    public void updateFill(MetaObjectHandler handler, MetaObject metaObject) {
        // 更新时的自动填充逻辑
        handler.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        handler.strictUpdateFill(metaObject, "updateUser", String.class, getCurrentUser());
    }
}
```

### 5. 字段加密 - AesEncryptHandler

通过 `AesEncryptHandler` 实现字段级 AES 加密：

```java
public class User {
    @TableField(typeHandler = AesEncryptHandler.class)
    private String mobile;
    
    @TableField(typeHandler = AesEncryptHandler.class)
    private String idCard;
}
```

## 配置说明

### 1. 基础配置

```yaml
mybatis-plus:
  # Mapper 扫描路径
  mapper-package: com.example.mapper
  
  # 数据库类型映射（可选）
  database-id-map:
    "Custom DB": "mysql"
```

### 2. 加密配置

```yaml
mybatis-plus:
  encrypt:
    enabled: true
    key: "1234567890123456"  # 16位密钥
```

### 3. 多租户配置

```yaml
mybatis-plus:
  tenant:
    enabled: true
    tenant-column: "tenant_id"  # 租户字段名
    ignore-tables:              # 忽略的表
      - "sys_config"
      - "sys_dict"
```

## 使用示例

### 1. 基本 CRUD 操作

```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public User getUserByCode(String userCode) {
        return userMapper.selectByCode(userCode);
    }
    
    public int batchInsertUsers(List<User> users) {
        return userMapper.insertAllBatch(users);
    }
    
    public boolean updateUserByCode(User user) {
        return userMapper.updateByCode(user) > 0;
    }
    
    public boolean existsUser(String userName) {
        return userMapper.exists(Wrappers.<User>lambdaQuery()
            .eq(User::getUserName, userName));
    }
}
```

### 2. 实体类定义

```java
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    @TableCodeField
    @TableField("user_code")
    private String userCode;
    
    @TableField("user_name")
    private String userName;
    
    @TableField(value = "mobile", typeHandler = AesEncryptHandler.class)
    private String mobile;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT)
    private String createUser;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;
}
```

### 3. 数据权限使用

```java
@Mapper
public interface UserMapper extends LambdaBaseMapper<User> {
    
    /**
     * 查询用户列表（子查询模式）
     */
    @Purview(type = {1, 2}, mode = Purview.Mode.SUB_QUERY)
    List<User> selectUserList(@Param("userName") String userName);
    
    /**
     * 按部门查询用户（内联模式）
     */
    @Purview(
        key = "T.dept_id",
        type = {1},
        mode = Purview.Mode.INNER,
        scheme = Purview.Scheme.CASCADE
    )
    List<User> selectUsersByDept(@Param("deptId") Long deptId);
    
    /**
     * 统计用户数量（统计模式）
     */
    @Purview(type = {1}, mode = Purview.Mode.STATISTICS)
    Long countUsers();
}
```

### 4. 自定义元数据填充

```java
@Component
public class UserMetaFiller implements EntityMetaFiller {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 获取当前用户（需要自己实现）
        String currentUser = getCurrentUser();
        
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createUser", String.class, currentUser);
        this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        String currentUser = getCurrentUser();
        
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateUser", String.class, currentUser);
    }
    
    private String getCurrentUser() {
        // 实现获取当前用户的逻辑
        return "system";
    }
}
```

## 核心依赖

```xml
<!-- MyBatis-Plus Spring Boot 3 Starter -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>

<!-- MyBatis-Plus JSQLParser -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-jsqlparser</artifactId>
</dependency>

<!-- P6Spy SQL 监控 -->
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
</dependency>

<!-- Lambda Cloud 核心模块 -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-core</artifactId>
</dependency>

<!-- Lambda Cloud 安全模块（可选） -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-security</artifactId>
    <optional>true</optional>
</dependency>
```

## 注意事项

1. **编码字段操作**：使用 `selectByCode`、`updateByCode`、`deleteByCode` 方法时，实体类必须有字段标注 `@TableCodeField` 注解

2. **批量插入**：
   - `insertAll` 方法会根据数据库类型自动选择合适的批量插入策略
   - `insertAllBatch` 方法会自动分批处理大量数据，默认每批 1000 条
   - 批量操作建议在事务中执行

3. **数据权限**：
   - 使用数据权限功能需要配合权限管理系统
   - 权限拦截器会自动修改 SQL 语句添加权限条件

4. **字段加密**：
   - 启用加密功能后，密钥配置必须是 16 位字符串
   - 加密字段在数据库中存储的是加密后的值
   - 查询时会自动解密返回原始值

5. **多租户**：
   - 租户功能启用后会自动在 SQL 中添加租户条件
   - 可以通过 `ignore-tables` 配置忽略某些表的租户过滤

6. **SQL 监控**：
   - P6Spy 会记录所有 SQL 执行情况
   - 生产环境建议关闭或调整日志级别

## 版本兼容性

- **Spring Boot**: 3.0+
- **MyBatis-Plus**: 3.5+
- **Java**: 17+
- **数据库**: MySQL 5.7+、Oracle 11g+、PostgreSQL 10+、H2、DB2、达梦数据库

## 更新日志

### v1.0.0-SNAPSHOT
- ✨ 初始版本发布
- 🔧 提供 MyBatis-Plus 自动配置
- 📝 实现 `LambdaBaseMapper` 扩展 Mapper
- 🔄 支持字段自动填充功能
- 🛡️ 实现 `@Purview` 数据权限控制
- 🏢 支持多租户数据隔离
- 🔐 提供 AES 字段加密功能
- 📊 集成 P6Spy SQL 监控
- 🗄️ 支持多种数据库类型
- ⚡ 优化批量插入性能
