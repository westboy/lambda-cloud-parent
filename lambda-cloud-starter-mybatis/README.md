# lambda-cloud-starter-mybatis

MyBatis-Plus扩展模块，提供企业级开发常用功能增强

## 功能特性

### 1. 自动配置
- 自动配置JdbcTypeForNull处理
- 自动注册GlobalMetaObjectHandler
- 自动识别数据库类型
- 自动配置分页插件

### 2. 自定义SQL注入器
扩展MyBatis-Plus的SQL注入器，提供以下方法：
- 批量插入：insertAll、insertAllBatch
- 编码字段操作：selectByCode、updateByCode、deleteByCode
- 存在性检查：exists

### 3. 自动填充
通过GlobalMetaObjectHandler实现：
- 插入时自动填充：createUser、createTime、delFlag
- 更新时自动填充：updateUser、updateTime

### 4. 数据权限
通过@Purview注解实现数据权限控制：
- 支持多种权限模式
- 支持预处理权限检查
- 支持权限类型映射
- 支持owner用户跳过权限检查

### 5. 多租户支持
通过TenantExpressionInterceptor实现租户数据隔离：
- 支持从参数Map或Bean中获取租户ID
- 支持自定义租户字段名

### 6. 字段加密
通过AesEncryptHandler实现字段级AES加密

## 配置说明

### 1. 基础配置
```yaml
lambda:
  mybatis:
    mapper-package: com.example.mapper # Mapper扫描路径
    encrypt:
      enabled: true # 启用加密
      secret: your-secret-key # 加密密钥
```

### 2. 数据权限配置
```java
@Mapper
public interface UserMapper extends LambdaBaseMapper<User> {
    @Purview(type = {1,2}, mode = Purview.Mode.SUB_QUERY)
    List<User> selectList();
}
```

### 3. 多租户配置
```java
@Configuration
public class MyBatisConfig {
    @Bean
    public TenantExpressionInterceptor tenantInterceptor() {
        return new TenantExpressionInterceptor("tenantId");
    }
}
```

## 使用示例

### 1. 基本CRUD
```java
@Autowired
private UserMapper userMapper;

// 根据编码查询
User user = userMapper.selectByCode("user001");

// 批量插入
List<User> users = ...;
userMapper.insertAllBatch(users);
```

### 2. 数据权限使用
```java
// 在Service层设置当前用户
PurviewUtils.setOperator(loginUser);

// 在Mapper接口添加@Purview注解
@Purview(type = {1,2}, mode = Purview.Mode.SUB_QUERY)
List<User> selectList();
```

### 3. 字段加密
```java
public class User {
    @TableField(typeHandler = AesEncryptHandler.class)
    private String mobile;
}
```

## 注意事项
1. 使用数据权限功能时需要确保设置了当前用户
2. 批量插入方法需要根据数据库类型选择合适的方法
3. 加密功能启用后需要确保密钥安全
