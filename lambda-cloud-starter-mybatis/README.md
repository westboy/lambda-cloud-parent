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

### 6. 多租户支持

实现方式：

```java
@Configuration
public class TenantConfig {
    @Bean
    public TenantLineHandler tenantLineHandler() {
        return new TenantHandler(mybatisPlusExtendProperties);
    }
}
```
#### 表达式租户拦截器 - TenantExpressionInterceptor

`TenantExpressionInterceptor` 是一个 MyBatis 拦截器，用于自动从方法参数中提取租户ID并设置到租户上下文中：

```java
@Bean
public TenantExpressionInterceptor tenantExpressionInterceptor() {
    return new TenantExpressionInterceptor("tenant_id");
}
```

**工作原理：**
1. **拦截 SQL 执行**：拦截 MyBatis 的 `update` 和 `query` 方法
2. **多源租户ID获取**：按优先级从以下位置获取租户ID：
   - 方法参数 Map 中的指定字段
   - 方法参数 Bean 的指定属性
   - 当前登录用户的租户ID
3. **自动设置上下文**：将获取到的租户ID设置到 `TenantContextHolder` 中
4. **自动清理**：方法执行完成后自动清理租户上下文

**支持的参数类型：**
- **Map 参数**：从 `MapperMethod.ParamMap` 中获取指定 key 的值
- **Bean 参数**：通过反射获取 Bean 对象的指定属性值
- **用户上下文**：从 `OperatorUtils.getOperator().getTenantId()` 获取

### 7. 租户上下文管理 - TenantContextHolder

`TenantContextHolder` 是线程安全的租户上下文管理器，用于在多租户环境中管理当前线程的租户信息：

#### 核心方法
```java
// 获取单例实例
TenantContextHolder holder = TenantContextHolder.getInstance();

// 设置当前租户ID
holder.setTenantId("tenant_001");

// 获取当前租户ID
String tenantId = TenantContextHolder.getCurrentTenantId();

// 安全执行带租户上下文的代码块
String result = TenantContextHolder.runWithTenant("tenant_001", () -> {
    // 在此代码块中，租户ID会自动设置为 tenant_001
    return userService.getUserList();
});
```

#### 特性说明
- **线程安全**：基于 `ThreadLocal` 实现，确保多线程环境下的数据隔离
- **自动清理**：实现了 `AutoCloseable` 接口，支持 try-with-resources 语法自动清理
- **非空校验**：设置租户ID时会进行非空校验，防止空值污染
- **单例模式**：采用静态内部类实现单例，确保全局唯一实例

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

### 3. 自定义元数据填充

```java
import com.lambda.cloud.core.utils.OperatorUtils;

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
        return OperatorUtils.getOperator().getName();
    }
}
```

### 4. TenantExpressionInterceptor 使用示例

#### Map 参数方式
```java
@Mapper
public interface UserMapper extends LambdaBaseMapper<User> {
    
    /**
     * 使用 Map 参数传递租户ID
     * 拦截器会自动从参数中提取 tenant_id
     */
    List<User> selectUsersByTenant(@Param("tenant_id") String tenantId, 
                                   @Param("userName") String userName);
    
    /**
     * 使用 ParamMap 传递多个参数
     */
    List<User> selectUsersWithCondition(Map<String, Object> params);
}
```

```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public List<User> getUsersByTenant(String tenantId, String userName) {
        // 拦截器会自动从参数中提取 tenant_id 并设置到上下文
        return userMapper.selectUsersByTenant(tenantId, userName);
    }
    
    public List<User> getUsersWithMap(String tenantId, String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("tenant_id", tenantId);  // 拦截器会提取这个值
        params.put("status", status);
        return userMapper.selectUsersWithCondition(params);
    }
}
```

#### Bean 参数方式
```java
@Data
public class UserQuery {
    private String tenantId;  // 拦截器会通过反射获取这个属性
    private String userName;
    private String status;
    private Date createTimeStart;
    private Date createTimeEnd;
}
```

```java
@Mapper
public interface UserMapper extends LambdaBaseMapper<User> {
    
    /**
     * 使用 Bean 参数传递租户ID
     * 拦截器会通过反射获取 UserQuery.tenantId 属性
     */
    List<User> selectUsersByQuery(UserQuery query);
}
```

```java
@Service
public class UserService {
    
    public List<User> searchUsers(String tenantId, String userName, String status) {
        UserQuery query = new UserQuery();
        query.setTenantId(tenantId);  // 拦截器会自动提取
        query.setUserName(userName);
        query.setStatus(status);
        
        return userMapper.selectUsersByQuery(query);
    }
}
```

#### 用户上下文方式
```java
@Service
public class UserService {
    
    /**
     * 当方法参数中没有租户ID时，
     * 拦截器会从当前登录用户上下文中获取租户ID
     */
    public List<User> getCurrentTenantUsers() {
        // 拦截器会调用 OperatorUtils.getOperator().getTenantId()
        return userMapper.selectList(null);
    }
}
```

#### 配置示例
```java
@Configuration
public class MyBatisConfig {
    
    /**
     * 配置租户表达式拦截器
     * 参数 "tenantId" 指定要提取的字段/属性名
     */
    @Bean
    public TenantExpressionInterceptor tenantExpressionInterceptor() {
        return new TenantExpressionInterceptor("tenantId");
    }
    
    /**
     * 如果租户字段名为其他名称，可以自定义
     */
    @Bean
    public TenantExpressionInterceptor customTenantInterceptor() {
        return new TenantExpressionInterceptor("orgId");  // 使用 orgId 作为租户字段
    }
}
```

### 5. 多租户上下文使用

#### 基本使用方式
```java
@Service
public class TenantService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 方式一：手动管理租户上下文
     */
    public List<User> getUsersByTenant(String tenantId) {
        try (TenantContextHolder holder = TenantContextHolder.getInstance()) {
            holder.setTenantId(tenantId);
            return userMapper.selectList(null);
        }
    }
    
    /**
     * 方式二：使用 runWithTenant 方法
     */
    public List<User> getUsersWithTenant(String tenantId) throws Exception {
        return TenantContextHolder.runWithTenant(tenantId, () -> {
            return userMapper.selectList(null);
        });
    }
    
    /**
     * 方式三：在 Controller 层设置租户上下文
     */
    public void processMultiTenantData() {
        List<String> tenantIds = Arrays.asList("tenant_001", "tenant_002", "tenant_003");
        
        for (String tenantId : tenantIds) {
            try (TenantContextHolder holder = TenantContextHolder.getInstance()) {
                holder.setTenantId(tenantId);
                
                // 处理当前租户的数据
                List<User> users = userMapper.selectList(null);
                log.info("Tenant {} has {} users", tenantId, users.size());
                
                // 执行其他业务逻辑
                processUsersForTenant(users);
            }
        }
    }
}
```

#### 在拦截器中使用
```java
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        // 从请求头或参数中获取租户ID
        String tenantId = request.getHeader("X-Tenant-Id");
        if (StringUtils.isNotBlank(tenantId)) {
            TenantContextHolder.getInstance().setTenantId(tenantId);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Object handler, Exception ex) throws Exception {
        // 请求完成后清理租户上下文
        try (TenantContextHolder holder = TenantContextHolder.getInstance()) {
            // 自动清理
        }
    }
}
```

#### 异步任务中的租户上下文传递
```java
@Service
public class AsyncTenantService {
    
    @Async
    public CompletableFuture<Void> processAsyncTask(String tenantId, List<Long> userIds) {
        return CompletableFuture.runAsync(() -> {
            try (TenantContextHolder holder = TenantContextHolder.getInstance()) {
                holder.setTenantId(tenantId);
                
                // 异步处理租户数据
                for (Long userId : userIds) {
                    User user = userMapper.selectById(userId);
                    // 处理用户数据
                    processUser(user);
                }
            }
        });
    }
    
    /**
     * 使用 runWithTenant 简化异步任务
     */
    @Async
    public CompletableFuture<List<User>> getAsyncUsers(String tenantId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TenantContextHolder.runWithTenant(tenantId, () -> {
                    return userMapper.selectList(null);
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to get users for tenant: " + tenantId, e);
            }
        });
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

3. **字段加密**：
   - 启用加密功能后，密钥配置必须是 16 位字符串
   - 加密字段在数据库中存储的是加密后的值
   - 查询时会自动解密返回原始值

4. **多租户**：
   - 租户功能启用后会自动在 SQL 中添加租户条件
   - 可以通过 `ignore-tables` 配置忽略某些表的租户过滤

6. **TenantContextHolder 使用**：
   - 推荐使用 try-with-resources 语法确保租户上下文自动清理
   - 在异步任务中需要手动传递租户上下文
   - 设置租户ID时会进行非空校验，传入 null 值会抛出异常
   - 多线程环境下每个线程的租户上下文是独立的

7. **TenantExpressionInterceptor 使用**：
   - 拦截器按优先级获取租户ID：参数 Map > 参数 Bean > 用户上下文
   - Bean 参数方式需要提供对应属性的 getter 方法
   - 拦截器名称参数必须与实际字段/属性名保持一致
   - 如果所有方式都无法获取到租户ID，则不会设置租户上下文
   - 拦截器会自动进行类型转换，将获取到的值转换为 String 类型

8. **SQL 监控**：
   - P6Spy 会记录所有 SQL 执行情况
   - 生产环境建议关闭或调整日志级别

