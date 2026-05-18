---
name: "lambda-cloud-entity-design"
description: "Lambda Cloud 实体建模与对象转换指南。当用户需要创建 Entity/DTO/VO、设计数据模型或配置对象转换器(@AutoConverter/@FieldMapping)时调用。"
---

# Lambda Cloud 实体建模与对象转换

## 模型层次体系

```
Entity (数据库实体)
  ├── BaseDO (审计字段基类: createUser/createTime/updateUser/updateTime)
  │
DTO (数据传输对象, 入参)
  ├── BaseDTO<E> (提供 toEntity() 转换)
  ├── BasePageDTO<T> (分页参数: pageNum/pageSize)
  │
VO (视图对象, 出参)
  └── BaseVO<E> (提供 fromEntity/fromEntityList 静态转换)
```

## BaseDO 使用

```java
@Data
@TableName("sys_user")
public class UserEntity extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String status;
    private String tenantId;
}
```

BaseDO 提供的审计字段会由 `EntityMetaFiller` 机制自动填充。

## BaseDTO 使用

```java
@Data
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;
    private String nickname;
    private String email;
}

// 转换为 Entity
UserEntity entity = userCreateDTO.toEntity();
```

## BaseVO 使用

```java
@Data
public class UserVO extends BaseVO<UserEntity> {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private LocalDateTime createTime;
}

// 单个转换
UserVO vo = BaseVO.fromEntity(UserVO.class, entity);

// 列表转换
List<UserVO> voList = BaseVO.fromEntityList(UserVO.class, entityList);
```

## 分页请求

```java
@Data
public class UserQueryDTO extends BasePageDTO<UserEntity> {
    private String username;
    private String status;
}

// 使用
Page<UserEntity> page = userQueryDTO.getPage();
```

分页默认值: `pageNum=1`, `pageSize=20`, `maxPageSize=1000`

## @AutoConverter 编译期转换

需要引入 `lambda-cloud-processor`:

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

### 基本用法

```java
@AutoConverter(target = UserEntity.class)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;
    private String nickname;
}
```

编译期会生成 `UserCreateDTOConverter` 接口:

```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = ConvertFunctions.class
)
public interface UserCreateDTOConverter extends BaseConverter<UserCreateDTO, UserEntity> {
}
```

### 反向转换

```java
@AutoConverter(target = UserEntity.class, isReverse = true)
public class UserVO extends BaseVO<UserEntity> {
    private Long id;
    private String username;
}
```

生成的接口继承 `BaseConverter<UserEntity, UserVO>`，即从 Entity 转为 VO。

### 自定义转换接口

```java
@AutoConverter(target = OrderEntity.class, converter = OrderConverter.class)
public class OrderCreateDTO extends BaseDTO<OrderEntity> {
    // ...
}

public interface OrderConverter extends BaseConverter<OrderCreateDTO, OrderEntity> {
    @AfterMapping
    default void afterConvert(OrderCreateDTO source, @MappingTarget OrderEntity target) {
        target.setStatus("PENDING");
    }
}
```

## @FieldMapping 字段映射

### 基本映射

```java
@AutoConverter(target = UserEntity.class)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;

    @FieldMapping(target = "status", defaultValue = "ACTIVE")
    private String status;

    @FieldMapping(target = "createTime", expression = "java(new java.time.LocalDateTime.now())")
    private LocalDateTime createTime;
}
```

### 类级别映射

```java
@AutoConverter(target = UserEntity.class)
@FieldMapping(source = "userName", target = "username")
public class ExternalUserDTO extends BaseDTO<UserEntity> {
    private String userName;
}
```

### 条件映射

```java
@FieldMapping(
    target = "phone",
    source = "phoneNumber",
    conditionExpression = "java(source.getPhoneNumber() != null && !source.getPhoneNumber().isEmpty())"
)
private String phone;
```

### 使用自定义函数

```java
@FieldMapping(target = "birthday", qualifiedByName = "formatDate")
private String birthdayStr;
```

`ConvertFunctions` 中定义:
```java
@Named("formatDate")
public LocalDate formatDate(String dateStr) {
    return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
}
```

## ConvertUtils 运行时转换

```java
// 单对象转换
UserVO vo = ConvertUtils.convert(userEntity, UserVO.class);

// 列表转换
List<UserVO> voList = ConvertUtils.convertList(userEntityList, UserVO.class);
```

## 设计规范

1. **DTO 命名**: `XxxCreateDTO`(创建) / `XxxUpdateDTO`(更新) / `XxxQueryDTO`(查询)
2. **VO 命名**: `XxxVO`(通用) / `XxxDetailVO`(详情) / `XxxListVO`(列表)
3. **Entity 命名**: `XxxEntity`，继承 `BaseDO`
4. **转换方向**: DTO -> Entity (toEntity), Entity -> VO (fromEntity)
5. **编译期优先**: 优先使用 @AutoConverter 编译期生成，性能优于运行时反射
6. **uses 扩展**: @AutoConverter 的 uses 属性可引入额外的转换工具类
