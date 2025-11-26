# Lambda Cloud Processor

编译时注解处理器，用于自动生成 MapStruct 转换器接口。

## 功能特性

- 基于 `@AutoConverter` 注解自动生成 MapStruct 转换器
- 支持字段映射配置
- 支持自定义转换器接口
- 编译时代码生成，无运行时性能损耗

## 依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

## 使用方式

### 基本用法

在需要生成转换器的类上添加 `@AutoConverter` 注解：

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO {
    private String name;
    private Integer age;
    // getter/setter...
}
```

编译后会自动生成 `UserDTOConverter` 接口：

```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = { ConvertFunction.class }
)
public interface UserDTOConverter extends BaseConverter<UserDTO, UserEntity> {
}
```

### 字段映射配置

#### 在注解中配置

```java
@AutoConverter(
    target = UserEntity.class,
    fieldMappings = {
        @FieldMapping(target = "userName", source = "name"),
        @FieldMapping(target = "userAge", source = "age")
    }
)
# Lambda Cloud Processor

编译时注解处理器，用于自动生成 MapStruct 转换器接口。

## 功能特性

- 基于 `@AutoConverter` 注解自动生成 MapStruct 转换器
- 支持字段映射配置
- 支持自定义转换器接口
- 编译时代码生成，无运行时性能损耗

## 依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

## 使用方式

### 基本用法

在需要生成转换器的类上添加 `@AutoConverter` 注解：

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO {
    private String name;
    private Integer age;
    // getter/setter...
}
```

编译后会自动生成 `UserDTOConverter` 接口：

```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = { ConvertFunction.class }
)
public interface UserDTOConverter extends BaseConverter<UserDTO, UserEntity> {
}
```

### 字段映射配置

#### 在注解中配置

```java
@AutoConverter(
    target = UserEntity.class,
    fieldMappings = {
        @FieldMapping(target = "userName", source = "name"),
        @FieldMapping(target = "userAge", source = "age")
    }
)
public class UserDTO {
    private String name;
    private Integer age;
}
```

#### 在类上配置

```java
@AutoConverter(target = UserEntity.class)
@FieldMapping(target = "userName", source = "name")
@FieldMappings({
    @FieldMapping(target = "userAge", source = "age"),
    @FieldMapping(target = "createTime", ignore = true)
})
public class UserDTO {
    // ...
}
```
> 支持 `@FieldMappings` 容器注解，可同时配置多个映射规则。

#### 在字段上配置

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO {
    
    @FieldMapping(target = "userName")
    private String name;
    
    @FieldMapping(target = "userAge")
    private Integer age;
    
    @FieldMapping(target = "createTime", ignore = true)
    private Date createTime;
}
```

### 继承关系与参数反转

#### 重要说明：BaseVO 继承的特殊处理

当类继承 `BaseVO` 时，生成的转换器接口会**自动反转泛型参数顺序**，这会影响 `@FieldMapping` 注解的使用：

##### 1. BaseDTO 继承（标准情况）
```java
@AutoConverter(target = UserEntity.class)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    @FieldMapping(target = "userName", source = "name")  // DTO字段 -> Entity字段
    private String name;
}

// 生成：BaseConverter<UserCreateDTO, UserEntity>
// 转换方向：DTO -> Entity
```

##### 2. BaseVO 继承（参数反转）
```java
@AutoConverter(target = UserEntity.class)
public class UserVO extends BaseVO<UserEntity> {
    @FieldMapping(target = "name", source = "userName")  // Entity字段 -> VO字段
    private String name;
}

// 生成：BaseConverter<UserEntity, UserVO>  ← 注意参数顺序反转
// 转换方向：Entity -> VO
```

#### FieldMapping 注解使用差异

| 继承类型 | 泛型参数 | 转换方向 | source 含义 | target 含义 |
|---------|---------|---------|------------|------------|
| BaseDTO | `<DTO, Entity>` | DTO → Entity | DTO字段名 | Entity字段名 |
| BaseVO | `<Entity, VO>` | Entity → VO | Entity字段名 | VO字段名 |
| 普通类 | `<源类, 目标类>` | 源 → 目标 | 源类字段名 | 目标类字段名 |

#### 实际应用场景

```java
// 场景1：接收前端数据，保存到数据库
@AutoConverter(target = UserEntity.class)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    @FieldMapping(target = "userName", source = "name")  // DTO.name -> Entity.userName
    private String name;
    
    @FieldMapping(target = "userAge", source = "age")    // DTO.age -> Entity.userAge
    private Integer age;
}

// 场景2：从数据库查询，返回给前端
@AutoConverter(target = UserEntity.class)
public class UserVO extends BaseVO<UserEntity> {
    @FieldMapping(target = "name", source = "userName")  // Entity.userName -> VO.name
    private String name;
    
    @FieldMapping(target = "age", source = "userAge")    // Entity.userAge -> VO.age
    private Integer age;
}
```

### 高级配置

#### 自定义转换器接口

```java
@AutoConverter(
    target = UserEntity.class,
    converter = CustomConverter.class
)
public class UserDTO {
    // ...
}
```

#### 使用其他转换器

```java
@AutoConverter(
    target = UserEntity.class,
    uses = { DateConverter.class, StringConverter.class }
)
public class UserDTO {
    // ...
}
```

#### 自定义 MapStruct 配置

```java
@AutoConverter(
    target = UserEntity.class,
    config = CustomMapperConfig.class
)
public class UserDTO {
    // ...
}
```

### 字段映射选项

`@FieldMapping` 注解支持以下属性：

- `target` - 目标字段名（必需）
- `source` - 源字段名
- `ignore` - 是否忽略该字段
- `dateFormat` - 日期格式
- `numberFormat` - 数字格式
- `expression` - 自定义表达式
- `defaultValue` - 默认值
- `qualifiedByName` - 指定转换方法名
- `conditionExpression` - 条件表达式
- `conditionQualifiedByName` - 条件限定方法名
- `qualifiedBy` - 限定注解类
- `conditionQualifiedBy` - 条件限定注解类

示例：

```java
@FieldMapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
@FieldMapping(target = "status", defaultValue = "ACTIVE")
@FieldMapping(target = "fullName", expression = "java(source.getFirstName() + ' ' + source.getLastName())")
```

## 生成规则

1. 生成的转换器接口名为：`{原类名}Converter`
2. 生成的接口位于与原类相同的包下
3. **泛型生成规则**：
   - `isReverse = false` (默认): 生成 `BaseConverter<DTO, Entity>`
   - `isReverse = true`: 生成 `BaseConverter<Entity, DTO>`
   - 若指定 `converter` 属性，则继承指定的接口
4. **依赖注入**：
   - 自动添加 `ConvertFunctions.class` 到 `uses` 属性
   - `@AutoConverter` 中配置的 `uses` 类会被追加到列表
   - 支持 `config` 属性指定 MapStruct 配置类

## 注意事项

- 该模块仅在编译时使用，建议设置 `scope` 为 `provided`
- 需要配合 `lambda-cloud-core` 模块使用
- 生成的转换器需要 MapStruct 运行时支持
- 确保目标类在编译路径中可访问