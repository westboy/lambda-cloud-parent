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
    private String name;
    private Integer age;
}
```

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

示例：

```java
@FieldMapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
@FieldMapping(target = "status", defaultValue = "ACTIVE")
@FieldMapping(target = "fullName", expression = "java(source.getFirstName() + ' ' + source.getLastName())")
```

## 生成规则

1. 生成的转换器接口名为：`{原类名}Converter`
2. 生成的接口位于与原类相同的包下
3. 如果原类继承自 `BaseDTO`，则生成 `BaseConverter<DTO, Entity>` 接口
4. 否则生成 `BaseConverter<Entity, DTO>` 接口
5. 自动添加 `ConvertFunction.class` 到 `uses` 属性

## 注意事项

- 该模块仅在编译时使用，建议设置 `scope` 为 `provided`
- 需要配合 `lambda-cloud-core` 模块使用
- 生成的转换器需要 MapStruct 运行时支持
- 确保目标类在编译路径中可访问