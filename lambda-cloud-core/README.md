# Lambda Cloud Core

项目核心模块，提供基础模型、工具类和异常处理等通用功能。

## 核心功能

### 1. 基础模型
- **BaseDO**: 数据对象基类，包含基础字段(id, createTime等)
- **BaseDTO**: 数据传输对象基类
- **BaseVO**: 视图对象基类
- **BasePageDTO**: 分页查询参数基类
- **BaseEnum**: 枚举基类接口

### 2. 对象转换
- **AutoConverter**: 自动生成转换接口注解
```java
@AutoConverter(target = UserEntity.class)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;
    
    @FieldMapping(target = "props", qualifiedByName = "mapToString")
    private Map<String, Object> props;
    
    @Named("mapToString")
    private String mapToString(Map<String, Object> map) {
        return map == null ? null : Constants.GSON.toJson(map);
    }
}
```
- **FieldMapping**: 字段映射注解，支持复杂映射规则
- **Named**: MapStruct注解，用于标记转换方法，支持在DTO类内定义转换逻辑
- **BaseConverter**: 转换接口基类，提供标准转换方法
- **ConvertFunctions**: 内置转换函数集合，支持以下类型转换：
  - Map ↔ String (JSON)
  - List ↔ String (JSON)
  - LocalDateTime/LocalDate ↔ String (ISO格式)
  - Number (Long, Integer, Double) ↔ String
  - Boolean ↔ String
  - Enum ↔ String
  - UUID ↔ String
- **ConverterResolver**: 转换器解析器，自动查找和缓存转换器
- **ConvertUtils**: 简化的转换工具入口
```java
// 自动查找转换器并执行转换
UserVO vo = ConvertUtils.convert(userEntity);
```

### 3. 工具类
- **Assert**: 参数校验工具
```java
 Assert.notNull(obj);
```
- **HmacGenerator**: HMAC签名生成器
```java
String signature = HmacGenerator.hmacSha256("data", "secret");
```
- **OperatorUtils**: 获取当前操作员信息
```java
String operator = OperatorUtils.getCurrentOperator();
```
- **TypeConverter**: 类型转换工具类，支持基本类型安全转换
- **ClassTypeUtils**: 类型检查与转换工具
```java
// 判断是否为基本类型或包装类型
boolean isPrimitive = ClassTypeUtils.isPrimitiveOrWrapper(Integer.class);
// 字符串转对应类型
Object value = ClassTypeUtils.convertPrimitiveOrWrapper(Integer.class, "123");
```
- **StpLogicUtils**: Sa-Token 多账号体系集成工具
```java
// 获取当前活跃的登录逻辑（自动识别 HMAC 或 普通登录）
StpLogic logic = StpLogicUtils.getActiveStpLogic();
// 根据 Token 获取会话
SaSession session = StpLogicUtils.getSaSession(token);
```

### 4. 异常处理
- **基础异常**:
  - IllegalAccessException
  - IllegalArgumentException
  - IllegalStateException
  - NotSupportedException

- **Feign异常**:
  - AbstractFeignException
  - FeignAccessDeniedException
  - FeignArgumentNotValidException
  - FeignInternalServerErrorException
  - FeignServiceNotAvailableException
  - FeignUnauthorizedException

- **异常模型**:
  - ErrorCode: 错误码枚举
  - ErrorModel: 错误响应模型
  - ArgumentError: 参数错误详情

### 5. Jackson定制
- **LambdaObjectMapper**: 定制ObjectMapper
- **序列化/反序列化**:
  - LambdaLocalDateTimeSerializer
  - LambdaLocalDateTimeDeserializer
- **ExtendDateFormat**: 扩展日期格式处理

### 6. 其他核心
- **Constants**: 全局常量定义
  - 认证常量：HMAC, LOGIN_USER 等
  - JSON工具：GSON 实例
  - 日期格式：DATE_TIME_PATTERN, ISO8601_PATTERN 等
  - 正则表达式：DATE_REGEX, TIME_STAMP_REGEX 等
- **CorsProperty**: CORS跨域配置属性
```yaml
lambda:
  core:
    cors:
      allowed-origins: "*"
      allowed-methods: "*"
```
- **LoginUser**: 登录用户信息模型

## 使用示例

### 基础模型继承
```java
public class UserDO extends BaseDO {
    private String username;
    private String password;
}
```

### 对象转换

#### 原理说明

本框架的对象转换基于 **MapStruct** 和 **注解处理器（Annotation Processor）** 技术，在编译期自动生成高性能的对象转换代码。

##### 1. 核心组件

- **@AutoConverter**：标记需要自动生成转换器的类
- **@FieldMapping**：配置字段映射规则
- **AutoConverterProcessor**：编译期注解处理器，负责生成 MapStruct Mapper 接口
- **ConvertFunction**：提供常用的类型转换方法
- **BaseConverter**：转换器基础接口
- **ConverterResolver**：转换器解析器，用于运行时获取转换器实例

##### 2. 工作流程

1. **编译期处理**：
   - `AutoConverterProcessor` 扫描带有 `@AutoConverter` 注解的类
   - 分析类的字段和 `@FieldMapping` 配置
   - 自动生成 MapStruct Mapper 接口（如 `UserCreateDTOConverter`）
   - 将 `ConvertFunction.class` 自动添加到 `uses` 中

2. **MapStruct 编译**：
   - MapStruct 处理器根据生成的 Mapper 接口
   - 生成具体的实现类，包含高效的字段赋值代码
   - 处理类型转换、字段映射、空值检查等

3. **运行时使用**：
   - Spring 自动注册生成的 Mapper 实现为 Bean
   - 通过 `BaseDTO.toEntity()` 或 `BaseVO.fromEntity()` 调用
   - 或通过 `ConverterResolver` 获取转换器实例

##### 3. 字段映射机制

- **同名字段**：自动映射，无需配置
- **不同名字段**：使用 `@FieldMapping(source = "sourceField", target = "targetField")`
- **类型转换**：通过 `qualifiedByName` 引用 `ConvertFunction` 中的转换方法
- **复杂映射**：支持表达式、条件映射、嵌套对象转换

##### 4. 性能优势

- **编译期生成**：避免运行时反射，性能接近手写代码
- **类型安全**：编译期检查，避免运行时类型错误
- **零运行时依赖**：生成的代码不依赖框架运行时库

```java
// 1. DTO转Entity示例 - 使用 @AutoConverter 自动生成转换器
@AutoConverter(target = UserEntity.class)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;
    
    // 直接使用 ConvertFunction 中定义的转换方法
    @FieldMapping(target = "props", qualifiedByName = "mapToString")
    private Map<String, Object> props;
}

// 使用生成的转换器
UserCreateDTO dto = new UserCreateDTO();
UserEntity entity = dto.toEntity();

// 2. Entity转VO示例 - 使用 @AutoConverter 自动生成转换器
@AutoConverter(target = UserEntity.class)
public class UserVO extends BaseVO<UserEntity> {
    private String username;
    private String createTime;
    
    // 直接使用 ConvertFunction 中定义的转换方法
    @FieldMapping(source = "props", qualifiedByName = "stringToMap")
    private Map<String, Object> properties;
}

// 使用生成的转换器
UserEntity entity = new UserEntity();
UserVO vo = UserVO.fromEntity(entity);

// 3. 使用转换器解析器
BaseConverter<UserCreateDTO, UserEntity> dtoConverter = 
    ConverterResolver.getConverter(UserCreateDTO.class);
UserEntity entity = dtoConverter.convertTo(dto);

BaseConverter<UserVO, UserEntity> voConverter = 
    ConverterResolver.getConverter(UserVO.class);
UserVO vo = voConverter.convertFrom(entity);
```

#### 重要说明：转换方向控制机制

**⚠️ 关键特性**：`@AutoConverter` 注解的 `isReverse` 属性控制生成的转换器接口的泛型参数顺序和转换方向。这个机制决定了转换器的 `convertTo` 方法的参数类型和返回类型，同时影响 `@FieldMapping` 注解中 `source` 和 `target` 字段的含义。

##### 转换方向控制规则

| isReverse 值 | 生成的转换器泛型 | convertTo 方法签名 | 转换方向 | 典型使用场景 |
|-------------|-----------------|-------------------|---------|-------------|
| `false`（默认） | `BaseConverter<Target, Source>` | `Source convertTo(Target input)` | Target → Source | DTO → Entity（接收前端数据，保存到数据库） |
| `true` | `BaseConverter<Source, Target>` | `Target convertTo(Source input)` | Source → Target | Entity → VO（从数据库查询，返回给前端） |

**核心理解**：
- `isReverse = false`：转换器接受 Target 类型参数，返回 Source 类型结果
- `isReverse = true`：转换器接受 Source 类型参数，返回 Target 类型结果

##### FieldMapping 注解使用差异

**核心区别**：`@FieldMapping` 注解中的 `source` 和 `target` 字段含义会根据 `isReverse` 属性发生变化。

**重要提示**：在 `@FieldMapping` 中：
- `source`：始终指向**输入对象**的字段（convertTo 方法的参数对象）
- `target`：始终指向**输出对象**的字段（convertTo 方法的返回对象）

###### 1. isReverse = false（默认情况）
```java
@AutoConverter(target = UserEntity.class)  // isReverse = false（默认）
public class UserCreateDTO extends BaseDTO<UserEntity> {
    // convertTo 方法：UserEntity convertTo(UserCreateDTO input)
    // source 指输入对象（UserCreateDTO）字段，target 指输出对象（UserEntity）字段
    @FieldMapping(source = "userName", target = "username")  // DTO.userName → Entity.username
    private String userName;
    
    @FieldMapping(source = "age", target = "userAge")        // DTO.age → Entity.userAge
    private Integer age;
}

// 生成的转换器：BaseConverter<UserCreateDTO, UserEntity>
// 生成的方法：UserEntity convertTo(UserCreateDTO input)
// 转换方向：UserCreateDTO → UserEntity
```

###### 2. isReverse = true（参数反转）
```java
@AutoConverter(target = UserEntity.class, isReverse = true)
public class UserVO extends BaseVO<UserEntity> {
    // convertTo 方法：UserVO convertTo(UserEntity input)
    // source 指输入对象（UserEntity）字段，target 指输出对象（UserVO）字段
    @FieldMapping(source = "username", target = "userName")  // Entity.username → VO.userName
    private String userName;
    
    @FieldMapping(source = "userAge", target = "age")        // Entity.userAge → VO.age
    private Integer age;
}

// 生成的转换器：BaseConverter<UserEntity, UserVO>
// 生成的方法：UserVO convertTo(UserEntity input)
// 转换方向：UserEntity → UserVO
```

##### 实际应用对比

**场景1：保存用户信息（isReverse = false，默认）**
```java
@AutoConverter(target = UserEntity.class)  // isReverse = false（默认）
public class UserCreateDTO extends BaseDTO<UserEntity> {
    // 输入：UserCreateDTO，输出：UserEntity
    @FieldMapping(source = "userName", target = "username")  // DTO.userName → Entity.username
    private String userName;
}

// 生成的方法：UserEntity convertTo(UserCreateDTO input)
// 使用：DTO → Entity
UserEntity entity = userCreateDTOConverter.convertTo(userCreateDTO);
```

**场景2：查询用户信息（isReverse = true）**
```java
@AutoConverter(target = UserVO.class, isReverse = true)
public class UserEntity extends BaseVO<UserVO> {
    // 输入：UserEntity，输出：UserVO
    @FieldMapping(source = "username", target = "userName")  // Entity.username → VO.userName
    private String username;
}

// 生成的方法：UserVO convertTo(UserEntity input)
// 使用：Entity → VO
UserVO vo = userEntityConverter.convertTo(userEntity);
```

##### 技术原理

这种转换方向控制机制在 `AutoConverterProcessor` 的第97行和第346行实现：

**1. 泛型参数顺序控制（第97行）**：
```java
// 核心判断逻辑
AutoConverter anno = typeElement.getAnnotation(AutoConverter.class);
ParameterizedTypeName superInterface;
if (anno.isReverse()) {
    // isReverse = true：反转参数顺序
    superInterface = ParameterizedTypeName.get(
            ClassName.get("com.lambda.cloud.core.convert", "BaseConverter"),
            ClassName.bestGuess(sourceClassName),      // Source 在前
            ClassName.bestGuess(targetMirror.toString())); // Target 在后
} else {
    // isReverse = false（默认）：标准参数顺序
    superInterface = ParameterizedTypeName.get(
            ClassName.get("com.lambda.cloud.core.convert", "BaseConverter"),
            ClassName.bestGuess(targetMirror.toString()),   // Target 在前
            ClassName.bestGuess(sourceClassName));         // Source 在后
}
```

**2. 转换方法参数控制（第346行）**：
```java
// 转换方法的参数和返回值
if (anno.isReverse()) {
    // isReverse = true：Source -> Target
    convertToBuilder
            .addParameter(ClassName.bestGuess(sourceClassName), "source")
            .returns(ClassName.bestGuess(targetClassName));
} else {
    // isReverse = false（默认）：Target -> Source
    convertToBuilder
            .addParameter(ClassName.bestGuess(targetClassName), "source")
            .returns(ClassName.bestGuess(sourceClassName));
}
```

**关键机制**：框架通过 `@AutoConverter` 注解的 `isReverse` 属性来控制转换方向，而不是通过类名后缀判断。当继承 `BaseVO` 时，通常需要设置 `isReverse = true` 来实现 Entity → VO 的转换方向。

##### 最佳实践建议

1. **明确转换方向**：在编写 `@FieldMapping` 注解前，先确认 `@AutoConverter` 的 `isReverse` 属性值和生成的 `convertTo` 方法签名
2. **记住核心原则**：
   - `source` 始终指向**输入对象**的字段（convertTo 方法的参数）
   - `target` 始终指向**输出对象**的字段（convertTo 方法的返回值）
3. **统一使用规范**：
   - DTO → Entity 转换：使用 `isReverse = false`（默认）
   - Entity → VO 转换：使用 `isReverse = true`
4. **注释说明**：在复杂的字段映射上添加注释，说明转换方向和字段对应关系
5. **测试验证**：编写单元测试验证字段映射的正确性

```java
// 推荐：明确的注释说明
@AutoConverter(target = UserVO.class, isReverse = true)  // Entity → VO
public class UserEntity extends BaseVO<UserVO> {
    @FieldMapping(source = "username", target = "userName")  // Entity.username → VO.userName
    private String username;
}
```

6. **理解字段映射本质**：
   - 不要依赖类名后缀判断转换方向
   - 始终以 `convertTo` 方法的实际签名为准
   - `source` 和 `target` 的含义完全由 `isReverse` 属性决定

6. **命名约定**：
   - 保持一致的命名规范，减少映射配置
   - 使用有意义的字段名，便于理解转换方向

7. **类型转换**：
   - 合理使用 `ConvertFunction` 中的转换方法
   - 注意日期、数字等类型的格式化需求

#### 生成的代码示例

以上 `UserCreateDTO` 会自动生成如下 MapStruct Mapper 接口：

```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = { ConvertFunction.class }
)
public interface UserCreateDTOConverter extends BaseConverter<UserCreateDTO, UserEntity> {
    
    @Mapping(target = "props", source = "props", qualifiedByName = "mapToString")
    UserEntity convertTo(UserCreateDTO source);
}
```

MapStruct 进一步生成实现类：

```java
@Component
public class UserCreateDTOConverterImpl implements UserCreateDTOConverter {
    
    @Override
    public UserEntity convertTo(UserCreateDTO source) {
        if (source == null) return null;
        
        UserEntity target = new UserEntity();
        target.setUsername(source.getUsername());
        target.setProps(ConvertFunction.mapToString(source.getProps()));
        return target;
    }
}
```

#### 高级配置

##### 1. 自定义转换器配置

```java
@AutoConverter(
    target = UserEntity.class,
    uses = { CustomConverter.class },  // 额外的转换器类
    config = CustomMapperConfig.class  // 自定义配置类
)
public class UserDTO extends BaseDTO<UserEntity> {
    // ...
}
```

##### 2. 复杂字段映射

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO extends BaseDTO<UserEntity> {
    
    // 字段名映射
    @FieldMapping(source = "userName", target = "username")
    private String userName;
    
    // 表达式映射
    @FieldMapping(target = "fullName", expression = "java(source.getFirstName() + ' ' + source.getLastName())")
    private String firstName;
    private String lastName;
    
    // 条件映射
    @FieldMapping(target = "status", conditionExpression = "java(source.getActive() != null)")
    private Boolean active;
    
    // 忽略字段
    @FieldMapping(target = "password", ignore = true)
    private String password;
    
    // 默认值
    @FieldMapping(target = "createTime", defaultExpression = "java(java.time.LocalDateTime.now())")
    private LocalDateTime createTime;
}
```

##### 3. 嵌套对象转换

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO extends BaseDTO<UserEntity> {
    
    // 嵌套对象自动转换（需要对应的转换器）
    @FieldMapping(target = "department")
    private DepartmentDTO department;
    
    // 集合转换
    @FieldMapping(target = "roles")
    private List<RoleDTO> roles;
}
```

#### 最佳实践

1. **继承基类**：DTO 继承 `BaseDTO`，VO 继承 `BaseVO`，享受框架提供的便利方法
2. **合理使用 ConvertFunction**：优先使用框架提供的转换方法，避免重复定义
3. **字段命名一致**：尽量保持源对象和目标对象的字段名一致，减少映射配置
4. **性能考虑**：避免在转换方法中进行复杂计算，考虑使用表达式或后处理
5. **类型安全**：充分利用编译期检查，避免运行时类型错误

#### @FieldMapping 注解详细说明

`@FieldMapping` 是本框架提供的字段映射注解，用于定义源对象和目标对象之间的字段映射关系。该注解在编译期会被转换为 MapStruct 的 `@Mapping` 注解。

##### 注解属性详解

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `target` | String | - | **必填**，目标字段名，指定映射到目标对象的字段名 |
| `source` | String | "" | 源字段名，如果为空则使用目标字段名作为源字段名 |
| `ignore` | boolean | false | 是否忽略该字段，设置为 true 时在映射过程中忽略该字段 |
| `qualifiedByName` | String | "" | 限定名称，用于指定映射方法的限定名称（如 ConvertFunction 中的方法） |
| `expression` | String | "" | Java 表达式，用于复杂映射的 Java 表达式 |
| `defaultExpression` | String | "" | 默认表达式，当源字段为 null 时使用的默认表达式 |
| `defaultValue` | String | "" | 默认值，当源字段为 null 时使用的默认值 |
| `conditionExpression` | String | "" | 条件表达式，用于条件映射的表达式 |
| `conditionQualifiedByName` | String | "" | 条件限定名称，用于条件映射的限定名称 |
| `dateFormat` | String | "" | 日期格式，用于日期类型字段的格式化 |
| `numberFormat` | String | "" | 数字格式，用于数字类型字段的格式化 |
| `locale` | String | "" | 区域设置，用于格式化的区域设置 |
| `qualifiedBy` | Class<?>[] | {} | 限定类，用于映射的限定类 |
| `conditionQualifiedBy` | Class<?>[] | {} | 条件限定类，用于条件映射的限定类 |

##### 使用位置

- **类级别**：在类上使用，定义该类的字段映射规则
- **字段级别**：在字段上使用，定义该字段的映射规则
- **可重复使用**：支持 `@Repeatable`，可在同一位置使用多个 `@FieldMapping`

##### 常用映射场景

###### 1. 基础字段映射

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO extends BaseDTO<UserEntity> {
    
    // 字段名不同时的映射
    @FieldMapping(source = "userName", target = "username")
    private String userName;
    
    // 忽略敏感字段
    @FieldMapping(target = "password", ignore = true)
    private String password;
}
```

###### 2. 类型转换映射

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO extends BaseDTO<UserEntity> {
    
    // 使用 ConvertFunction 中的转换方法
    @FieldMapping(target = "props", qualifiedByName = "mapToString")
    private Map<String, Object> props;
    
    // 日期格式化
    @FieldMapping(target = "birthDate", dateFormat = "yyyy-MM-dd")
    private String birthDate;
    
    // 数字格式化
    @FieldMapping(target = "salary", numberFormat = "#.##")
    private String salary;
}
```

###### 3. 表达式映射

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO extends BaseDTO<UserEntity> {
    
    // 简单表达式
    @FieldMapping(target = "fullName", 
                  expression = "java(source.getFirstName() + ' ' + source.getLastName())")
    private String firstName;
    private String lastName;
    
    // 复杂表达式
    @FieldMapping(target = "ageGroup", 
                  expression = "java(source.getAge() >= 18 ? \"成年\" : \"未成年\")")
    private Integer age;
}
```

###### 4. 默认值映射

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO extends BaseDTO<UserEntity> {
    
    // 字符串默认值
    @FieldMapping(target = "status", defaultValue = "ACTIVE")
    private String status;
    
    // 表达式默认值
    @FieldMapping(target = "createTime", 
                  defaultExpression = "java(java.time.LocalDateTime.now())")
    private LocalDateTime createTime;
}
```

###### 5. 条件映射

```java
@AutoConverter(target = UserEntity.class)
public class UserDTO extends BaseDTO<UserEntity> {
    
    // 条件表达式
    @FieldMapping(target = "displayName", 
                  source = "nickname",
                  conditionExpression = "java(source.getNickname() != null && !source.getNickname().isEmpty())")
    private String nickname;
    
    // 条件限定名称
    @FieldMapping(target = "processedData", 
                  source = "rawData",
                  conditionQualifiedByName = "isValidData")
    private String rawData;
}
```

###### 6. 类级别映射

```java
@AutoConverter(target = UserEntity.class)
@FieldMapping(target = "createUser", defaultValue = "system")
@FieldMapping(target = "updateUser", defaultValue = "system")
@FieldMapping(target = "deleted", defaultValue = "false")
public class UserDTO extends BaseDTO<UserEntity> {
    private String username;
    private String email;
    // 类级别的映射会应用到所有转换方法
}
```

##### 优先级规则

1. **字段级别** > **类级别**：字段上的 `@FieldMapping` 优先于类上的配置
2. **显式配置** > **自动映射**：有 `@FieldMapping` 配置的字段优先于同名字段自动映射
3. **ignore = true** > **其他配置**：忽略字段的优先级最高

##### 注意事项

- `target` 属性是必填的，必须指定目标字段名
- `source` 为空时，默认使用 `target` 的值作为源字段名
- `expression` 和 `qualifiedByName` 不能同时使用
- `defaultValue` 和 `defaultExpression` 不能同时使用
- 条件映射只在条件为 true 时执行映射
- 日期和数字格式化需要对应的类型支持

#### 注意事项

- 转换器在编译期生成，修改注解后需要重新编译
- `@FieldMapping` 可以标注在类上、字段上，支持多种配置方式
- 生成的转换器自动注册为 Spring Bean，支持依赖注入
- 支持双向转换，DTO ↔ Entity，Entity ↔ VO

### 异常处理
```java
throw new FeignServiceNotAvailableException("服务不可用");
```

### Jackson配置
```java
@Bean
public LambdaObjectMapper lambdaObjectMapper() {
    return new LambdaObjectMapper();
}
```

## 依赖
- Spring Boot Starter
- Jackson Databind
- Lombok
- Spring Cloud OpenFeign
- MapStruct (对象映射)
- Gson (JSON处理)
- MyBatis-Plus (数据库操作)

## 注意事项
1. 基础模型类需配合Lombok使用
2. Feign异常需在Feign拦截器中处理
3. Jackson定制需通过LambdaObjectMapper生效
4. AutoConverter注解需要在编译时处理，确保注解处理器正确配置
5. @Named方法可以是静态方法或实例方法，推荐使用静态方法
6. FieldMapping支持复杂的映射表达式和条件映射
7. 转换器会自动缓存，避免重复创建实例
