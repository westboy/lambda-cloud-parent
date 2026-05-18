---
name: "lambda-cloud-core"
description: "Lambda Cloud 项目结构导航与核心基础能力指南。当用户询问项目架构、模块功能、通用模型(BaseDO/BaseDTO/BaseVO)、对象转换(@AutoConverter)、异常体系或工具类时调用。"
---

# Lambda Cloud Core 核心能力指南

## 模块定位

`lambda-cloud-core` 是 `lambda-cloud-parent` 的基础公共模块，提供模型基类、对象转换、异常模型、安全上下文与通用工具能力，供上层 starter 和业务模块直接复用。

**技术栈版本**:

| 技术 | 版本 |
|------|------|
| Java | 21 (虚拟线程) |
| Spring Boot | 4.0.2 |
| Spring Cloud | 2025.1.1 |
| Spring Cloud Alibaba | 2025.1.0.0 |
| MyBatis-Plus | 3.5.15 |
| Sa-Token | 1.45.0 |
| Dubbo | 3.3.6 |
| MapStruct | 最新版 |

## 包结构

```text
src/main/java/com/lambda/cloud/core
├─ annotation/          # @AutoConverter / @FieldMapping
├─ convert/             # BaseConverter / ConvertFunctions / ConverterResolver
├─ shared/              # BaseDO / BaseDTO / BaseVO / BasePageDTO / PageRequest / KeyValue / Convertible / BaseEnum
├─ principal/           # LoginUser / AnonymousUser
├─ utils/               # Assert / ConvertUtils / OperatorUtils / StpLogicUtils / HmacGenerator / TypeConverter / ClassTypeUtils
├─ exception/           # 基础异常、Feign异常、ErrorModel
├─ jackson/             # JacksonModuleConfigurer / ExtendDateFormat
└─ Constants.java       # 全局常量
```

---

## 一、通用模型体系

源码路径: `lambda-cloud-core/src/main/java/com/lambda/cloud/core/shared/`

### 1.1 BaseDO

数据库实体基类，提供统一的审计字段。

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseDO implements Serializable {

    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private String updateUser;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
```

**使用示例**:

```java
public class UserEntity extends BaseDO {
    private String username;
    private String email;
}
```

### 1.2 BaseDTO\<E\>

数据传输对象基类，提供 `toEntity()` 转换和查询构造器。

```java
@Data
public abstract class BaseDTO<E> {

    @Hidden
    @JsonIgnore
    public E toEntity();

    @Hidden
    @JsonIgnore
    public LambdaQueryWrapper<E> lambdaQueryWrapper();
}
```

**使用示例**:

```java
@AutoConverter(target = UserEntity.class, isReverse = true)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;

    @FieldMapping(target = "status", defaultValue = "ACTIVE")
    private String status;
}

// DTO -> Entity
UserEntity entity = dto.toEntity();
```

### 1.3 BaseVO\<E\>

视图对象基类，提供静态转换入口。

```java
public abstract class BaseVO<E> {

    public static <V extends BaseVO<E>, E> V fromEntity(Class<V> voClass, E entity);

    public static <V extends BaseVO<E>, E> List<V> fromEntityList(Class<V> voClass, List<E> entityList);
}
```

**使用示例**:

```java
// 单个转换
UserVO vo = BaseVO.fromEntity(UserVO.class, entity);

// 列表转换
List<UserVO> voList = BaseVO.fromEntityList(UserVO.class, entityList);
```

### 1.4 BasePageDTO\<T\>

分页请求基类，实现 `PageRequest<T>` 接口。

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BasePageDTO<T> implements PageRequest<T> {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 1000;
    public static final int MIN_PAGE_SIZE = 1;

    @NotNull
    @Min(value = 1, message = "页码必须大于等于1")
    protected Integer pageNum = DEFAULT_PAGE_NUM;

    @NotNull
    @Min(value = MIN_PAGE_SIZE, message = "每页条数必须大于等于1")
    @Max(value = MAX_PAGE_SIZE, message = "每页条数不能超过1000")
    protected Integer pageSize = DEFAULT_PAGE_SIZE;

    @JsonIgnore
    public LambdaQueryWrapper<T> getLambdaQueryWrapper();
}
```

### 1.5 PageRequest\<T\>

分页请求接口，提供 `getPage()` 方法生成 MyBatis-Plus 分页对象。

```java
public interface PageRequest<T> extends Serializable {

    Integer getPageSize();

    Integer getPageNum();

    @JsonIgnore
    default IPage<T> getPage() {
        return new Page<>(getPageNum(), getPageSize());
    }
}
```

**使用示例**:

```java
public class UserPageDTO extends BasePageDTO<User> {
    private String username;
    private Integer status;

    public LambdaQueryWrapper<User> buildQueryWrapper() {
        LambdaQueryWrapper<User> wrapper = getLambdaQueryWrapper();
        wrapper.like(StringUtils.hasText(username), User::getUsername, username)
               .eq(status != null, User::getStatus, status);
        return wrapper;
    }
}

// Service 层使用
IPage<User> page = userMapper.selectPage(userPageDTO.getPage(), userPageDTO.buildQueryWrapper());
```

### 1.6 Convertible\<T\>

可转换标记接口，标识对象支持自定义转换。

```java
public interface Convertible<T> {
    T convertTo();
}
```

### 1.7 BaseEnum\<I\>

枚举基接口，为所有业务枚举提供统一的编码获取规范。

```java
public interface BaseEnum<I> {
    I getCode();
}
```

**使用示例**:

```java
public enum UserStatus implements BaseEnum<Integer> {
    ACTIVE(1, "激活"),
    INACTIVE(0, "未激活"),
    LOCKED(-1, "锁定");

    private final Integer code;
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}
```

### 1.8 KeyValue

键值对模型，用于下拉选项、字典数据等场景。

```java
@Data
public class KeyValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String desc;

    public KeyValue() {}
    public KeyValue(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

---

## 二、对象转换体系

源码路径: `lambda-cloud-core/src/main/java/com/lambda/cloud/core/annotation/` 和 `convert/`

### 2.1 @AutoConverter 注解

声明式转换注解，标注在类上，由 `lambda-cloud-processor` 编译期处理生成 MapStruct Converter 接口。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `target` | `Class<?>` | 必填 | 转换目标类型 |
| `isReverse` | `boolean` | `false` | 转换方向控制 |
| `converter` | `Class<?>` | `Void.class` | 自定义父转换接口 |
| `uses` | `Class<?>[]` | `{}` | MapStruct 辅助类 |
| `config` | `Class<?>` | `void.class` | MapStruct 公共配置类 |
| `fieldMappings` | `FieldMapping[]` | `{}` | 字段映射配置 |

**isReverse 方向说明**:

| 值 | 生成结果 |
|---|---|
| `false` (默认) | 生成 `BaseConverter<Target, Source>`，`convertTo(Target source) -> Source` |
| `true` | 生成 `BaseConverter<Source, Target>`，`convertTo(Source source) -> Target` |

**使用示例**:

```java
// 默认方向: isReverse=false 生成 BaseConverter<UserEntity, UserCreateDTO>
@AutoConverter(target = UserEntity.class)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;
}

// 反向: isReverse=true 生成 BaseConverter<UserCreateDTO, UserEntity>
@AutoConverter(target = UserEntity.class, isReverse = true)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;
}
```

### 2.2 @FieldMapping 注解

声明字段映射规则，可应用在类或字段上，支持 `@Repeatable`。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `target` | `String` | 必填 | 目标字段名 |
| `source` | `String` | `""` | 源字段名（空则同名映射） |
| `ignore` | `boolean` | `false` | 是否忽略该字段 |
| `defaultValue` | `String` | `""` | 源字段为 null 时的默认值 |
| `dateFormat` | `String` | `""` | 日期格式化 |
| `numberFormat` | `String` | `""` | 数字格式化 |
| `expression` | `String` | `""` | Java 表达式映射 |
| `defaultExpression` | `String` | `""` | 源字段为 null 时的默认表达式 |
| `qualifiedByName` | `String` | `""` | 映射方法限定名称 |
| `qualifiedBy` | `Class<?>[]` | `{}` | 映射限定类 |
| `conditionQualifiedBy` | `Class<?>[]` | `{}` | 条件限定类 |
| `conditionExpression` | `String` | `""` | 条件表达式 |
| `conditionQualifiedByName` | `String` | `""` | 条件限定名称 |
| `locale` | `String` | `""` | 区域设置 |

**使用示例**:

```java
@AutoConverter(target = UserEntity.class, isReverse = true)
public class UserCreateDTO extends BaseDTO<UserEntity> {

    @FieldMapping(target = "name", source = "username")
    private String username;

    @FieldMapping(target = "status", defaultValue = "ACTIVE")
    private String status;

    @FieldMapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private String createTime;
}
```

### 2.3 BaseConverter\<S, T\>

统一转换器接口，所有编译期生成的 Converter 均继承此接口。

```java
public interface BaseConverter<S, T> {

    T convertTo(S source);

    List<T> convertToList(List<S> sourceList);

    default <C extends Collection<T>> C convertToCollection(List<S> sourceList);

    default Optional<T> convertToOptional(S source);

    default List<T> convertNonNullToList(List<S> sourceList);

    default Set<T> convertToSet(List<S> sourceList);
}
```

| 方法 | 说明 | 空值处理 |
|------|------|----------|
| `convertTo(S source)` | 单对象转换 | 输入 null 返回 null |
| `convertToList(List<S>)` | 列表转换 | null 或空返回空列表 |
| `convertToCollection(List<S>)` | 转换为指定集合 | null 或空返回空 ArrayList |
| `convertToOptional(S source)` | 转换为 Optional | null 包装为 Optional.empty() |
| `convertNonNullToList(List<S>)` | 过滤空对象后转换 | 过滤 null 源和 null 结果 |
| `convertToSet(List<S>)` | 转换为 Set 集合 | 过滤 null 源和 null 结果 |

### 2.4 ConverterResolver

运行时转换器解析器，按命名约定查找 Spring Bean 并内置缓存。

**工作机制**:

1. 传入源对象的 `Class`
2. 拼接转换器类名: `源类型全限定名 + "Converter"`
3. 通过 `Class.forName()` 查找转换器类
4. 校验是否为 `BaseConverter` 实现
5. 通过 `SpringUtil.getBean()` 获取 Bean 实例
6. 使用 `ConcurrentHashMap` 缓存，避免重复查找

```java
public class ConverterResolver {

    private static final Map<Class<?>, BaseConverter<?, ?>> CACHE = new ConcurrentHashMap<>();

    public static <D, E> BaseConverter<D, E> getConverter(Class<?> clazz);
}
```

### 2.5 ConvertUtils

对象转换快捷入口，不需要传入目标类型参数，通过源对象类型自动推断。

```java
public class ConvertUtils {

    public static <S> S convert(S source);

    public static <S> List<S> convertList(List<S> source);
}
```

| 方法 | 说明 | 空值处理 |
|------|------|----------|
| `convert(S source)` | 单对象转换，自动查找对应 Converter | 输入 null 返回 null |
| `convertList(List<S> source)` | 列表转换，取首个元素类型查找 Converter | null 返回 null，空列表返回 `List.of()` |

### 2.6 ConvertFunctions

MapStruct `@Named` 转换函数库，提供常用的类型转换函数，可在 `@FieldMapping(qualifiedByName = "...")` 中引用。

| 分类 | 方法 | Named 名称 | 说明 |
|------|------|------------|------|
| **Map/String** | `mapToString(Map)` | `mapToString` | Map 转 JSON 字符串 |
| | `stringToMap(String)` | `stringToMap` | JSON 字符串转 Map |
| **List/String** | `listToString(List)` | `listToString` | List 转 JSON 字符串 |
| | `stringToList(String, Class)` | `stringToList` | JSON 字符串转 List（需指定元素类型） |
| **LocalDateTime** | `localDateTimeToString(LocalDateTime)` | `localDateTimeToString` | ISO_LOCAL_DATE_TIME 格式 |
| | `stringToLocalDateTime(String)` | `stringToLocalDateTime` | 解析 ISO_LOCAL_DATE_TIME |
| **LocalDate** | `localDateToString(LocalDate)` | `localDateToString` | ISO_LOCAL_DATE 格式 |
| | `stringToLocalDate(String)` | `stringToLocalDate` | 解析 ISO_LOCAL_DATE |
| **Long** | `longToString(Long)` | `longToString` | Long 转 String |
| | `stringToLong(String)` | `stringToLong` | String 转 Long |
| **Integer** | `integerToString(Integer)` | `integerToString` | Integer 转 String |
| | `stringToInteger(String)` | `stringToInteger` | String 转 Integer |
| **Double** | `doubleToString(Double)` | `doubleToString` | Double 转 String |
| | `stringToDouble(String)` | `stringToDouble` | String 转 Double |
| **Boolean** | `booleanToString(Boolean)` | `booleanToString` | Boolean 转 `"true"/"false"` |
| | `stringToBoolean(String)` | `stringToBoolean` | `"true"/"false"` 转 Boolean |
| **Enum** | `enumToString(Enum)` | `enumToString` | 枚举转 `name()` 字符串 |
| | `stringToEnum(String, Class)` | `stringToEnum` | 字符串转枚举实例 |
| **UUID** | `uuidToString(UUID)` | `uuidToString` | UUID 转 String |
| | `stringToUUID(String)` | `stringToUUID` | String 转 UUID |

所有方法均对 null 值做了安全处理，输入 null 返回 null。

**使用示例**:

```java
@AutoConverter(target = UserEntity.class, isReverse = true)
public class UserCreateDTO extends BaseDTO<UserEntity> {

    @FieldMapping(target = "metadata", qualifiedByName = "mapToString")
    private Map<String, Object> metadata;

    @FieldMapping(target = "tags", qualifiedByName = "listToString")
    private List<String> tags;

    @FieldMapping(target = "status", qualifiedByName = "stringToEnum")
    private String status;
}
```

---

## 三、异常模型体系

源码路径: `lambda-cloud-core/src/main/java/com/lambda/cloud/core/exception/`

### 3.1 基础异常

| 异常类 | HTTP 状态码 | 说明 |
|--------|-------------|------|
| `IllegalArgumentException` | 400 | 参数非法 |
| `IllegalAccessException` | 403 | 访问被拒绝 |
| `IllegalStateException` | 500 | 状态异常 |
| `NotSupportedException` | 501 | 不支持的操作 |

### 3.2 ErrorModel

统一错误响应模型。

```java
public class ErrorModel {
    private int status;
    private long timestamp;
    private String error;
    private String message;
    private List<ArgumentError> errors;
    private String path;
}
```

### 3.3 ArgumentError

参数错误信息模型。

```java
public class ArgumentError implements Serializable {
    private String field;
    private String details;
}
```

### 3.4 Feign 远程异常

用于 Feign 调用错误解码，继承 `AbstractFeignException`。

| 异常类 | HTTP 状态码 | 说明 |
|--------|-------------|------|
| `FeignArgumentNotValidException` | 400 | 参数校验失败 |
| `FeignUnauthorizedException` | 401 | 未授权 |
| `FeignAccessDeniedException` | 403 | 访问被拒绝 |
| `FeignInternalServerErrorException` | 500 | 服务端错误 |
| `FeignServiceNotAvailableException` | 503 | 服务不可用 |

---

## 四、安全上下文

源码路径: `lambda-cloud-core/src/main/java/com/lambda/cloud/core/principal/` 和 `utils/`

### 4.1 LoginUser

统一登录用户主体接口。

### 4.2 AnonymousUser

匿名用户默认实现，具有最小权限，账户被锁定且已过期。在用户未登录时作为安全降级返回。

### 4.3 OperatorUtils

操作员工具类，基于 Sa-Token 框架获取当前登录用户。

```java
public class OperatorUtils {

    public static LoginUser getOperator();

    public static LoginUser getSafeOperator();

    public static LoginUser getLoginUser(StpLogic stpLogic);

    public static <T extends LoginUser> T getLoginUser(Class<T> userClass);
}
```

| 方法 | 说明 | 未登录行为 |
|------|------|------------|
| `getOperator()` | 获取当前登录用户 | 抛出异常 |
| `getSafeOperator()` | 安全获取当前登录用户 | 返回 `ANONYMOUS_USER`（永不为 null） |
| `getLoginUser(StpLogic)` | 基于指定 StpLogic 获取用户 | 从 Token 会话中提取，未登录时行为由 StpLogic 决定 |
| `getLoginUser(Class<T>)` | 获取指定类型的登录用户 | 类型不匹配抛出 `ClassCastException` |

**使用示例**:

```java
// 安全获取（推荐）
LoginUser user = OperatorUtils.getSafeOperator();

// 强制获取（需确保已登录）
LoginUser user = OperatorUtils.getOperator();

// 获取指定类型
AdminUser admin = OperatorUtils.getLoginUser(AdminUser.class);
```

### 4.4 StpLogicUtils

Sa-Token 多登录类型管理工具，支持多种登录方式并存（如 HMAC 认证、标准用户登录等）。

默认已注册的登录类型: `hmac`、`login`（StpUtil 默认类型）。

```java
public class StpLogicUtils {

    public static void initializeLoginTypes(Collection<String> loginTypes);

    public static boolean containsLoginType(String loginType);

    public static StpLogic getByLoginType(String loginType);

    public static StpLogic getStpLogic(String loginType);

    public static SaSession getSaSession(String tokenValue);

    public static StpLogic getActiveStpLogic();

    public static void logoutByTokenValue(String tokenValue);
}
```

| 方法 | 说明 |
|------|------|
| `initializeLoginTypes(Collection<String>)` | 注册自定义登录类型，建议在启动时调用 |
| `containsLoginType(String)` | 检查是否支持指定登录类型，null/空返回 true |
| `getByLoginType(String)` | 按类型获取 StpLogic，不支持则抛出 `SaTokenException` |
| `getStpLogic(String)` | 获取 StpLogic，null/空返回默认实例 |
| `getSaSession(String)` | 根据令牌遍历所有登录类型查找会话 |
| `getActiveStpLogic()` | 自动检测当前活跃登录逻辑，遍历所有类型 |
| `logoutByTokenValue(String)` | 根据令牌在所有登录类型中注销 |

---

## 五、工具类

### 5.1 Assert

统一断言工具类，验证失败时抛出项目自定义异常（`IllegalArgumentException` / `IllegalStateException`）。

#### 状态断言

```java
static void state(boolean expression, String message);
static void state(boolean expression, Supplier<String> messageSupplier);
```

验证程序状态，失败抛出 `IllegalStateException`。

#### 逻辑断言

```java
static void isTrue(boolean expression, String message);
static void isTrue(boolean expression, Supplier<String> messageSupplier);
static void isFalse(boolean expression, String message);
static void isFalse(boolean expression, Supplier<String> messageSupplier);
```

验证逻辑条件，失败抛出 `IllegalArgumentException`。

#### 空值断言

```java
static void isNull(Object object, String message);
static void isNull(Object object, Supplier<String> messageSupplier);
static void notNull(Object object, String message);
static void notNull(Object object, Supplier<String> messageSupplier);
```

验证对象是否为 null，失败抛出 `IllegalArgumentException`。

#### 字符串断言

```java
static void hasLength(String text, String message);
static void hasText(String text, String message);
static void doesNotContain(String textToSearch, String substring, String message);
static void isBlank(String text, String message);
```

| 方法 | 说明 |
|------|------|
| `hasLength` | 字符串不为 null 且长度 > 0（允许空白） |
| `hasText` | 字符串包含至少一个非空白字符 |
| `doesNotContain` | 文本不包含指定子字符串 |
| `isBlank` | 字符串不为空白（null/空/纯空白） |

#### 集合断言

```java
static void notEmpty(Object[] array, String message);
static void noNullElements(Object[] array, String message);
static void notEmpty(Collection<?> collection, String message);
static void notEmpty(Map<?, ?> map, String message);
```

| 方法 | 说明 |
|------|------|
| `notEmpty(Object[])` | 数组不为 null 且包含元素 |
| `noNullElements(Object[])` | 数组不包含 null 元素 |
| `notEmpty(Collection)` | 集合不为 null 且包含元素 |
| `notEmpty(Map)` | Map 不为 null 且包含条目 |

#### 类型断言

```java
static void isInstanceOf(Class<?> type, Object obj, String message);
static void isAssignable(Class<?> superType, Class<?> subType, String message);
```

| 方法 | 说明 |
|------|------|
| `isInstanceOf` | 对象是指定类型的实例 |
| `isAssignable` | 子类型可分配给超类型 |

**使用示例**:

```java
Assert.notNull(user, "用户对象不能为空");
Assert.hasText(username, "用户名不能为空");
Assert.isTrue(age > 0, "年龄必须大于0");
Assert.isFalse(isDeleted, "用户已被删除，无法执行操作");
Assert.notEmpty(userList, "用户列表不能为空");
Assert.state(isInitialized, "系统尚未初始化");
```

### 5.2 HmacGenerator

HMAC-SHA1 签名生成器，用于 API 接口安全认证。

```java
public final class HmacGenerator {

    public static String baseString(String appId, long timestamp, Map<String, String[]> parameters, String body);

    public static String authorization(String appId, String secret, long timestamp, String baseString);
}
```

| 方法 | 参数 | 说明 |
|------|------|------|
| `baseString` | `appId`, `timestamp`, `parameters`, `body` | 构造基础签名字符串 |
| `authorization` | `appId`, `secret`, `timestamp`, `baseString` | 构造 HMAC 认证头 |

**签名流程**:

1. 查询参数按键名排序，拼接为 `key=value&` 格式
2. 追加 `appid=xxx&timestamp=xxx`
3. 如有请求体，清理空白后追加 `&body=xxx`
4. 对整个字符串进行 URL 解码
5. 使用 HMAC-SHA1 和 secret 签名，Base64 编码
6. 认证头格式: `HmacSHA {appId}:{timestamp}:{signature}`

**使用示例**:

```java
// 构造基础字符串
String base = HmacGenerator.baseString(appId, timestamp, queryMap, requestBody);

// 生成认证头
String authorization = HmacGenerator.authorization(appId, secret, timestamp, base);
```

### 5.3 TypeConverter

基本类型转换工具，支持 String、Long、Integer、Double、Boolean、BigDecimal、Date 之间的相互转换。

```java
public class TypeConverter {

    public static Object convert(Object value, Class<?> expect);
}
```

**转换规则**:

| 目标类型 | 支持的源类型 |
|----------|-------------|
| `String` | Long、Integer 及所有对象的 `toString()` |
| `Long` | 数值类型 |
| `Integer` | int、double、BigDecimal、Long |
| `Double` | double |
| `Boolean` | Integer、String |
| `BigDecimal` | String、double、int、Long |
| `Date` | Instant |

**使用示例**:

```java
Integer intValue = (Integer) TypeConverter.convert("123", Integer.class);
Long longValue = (Long) TypeConverter.convert(123.45, Long.class);
String strValue = (String) TypeConverter.convert(123, String.class);
Boolean boolValue = (Boolean) TypeConverter.convert(1, Boolean.class);
```

### 5.4 ClassTypeUtils

基本类型与包装类型工具类。

```java
public class ClassTypeUtils {

    public static boolean isPrimitiveOrWrapper(Class<?> clazz);

    public static Object convertPrimitiveOrWrapper(Class<?> targetClass, String content);
}
```

支持的类型: Integer、Long、Double、Float、Short、Byte、Boolean、Character 及对应基本类型。

**使用示例**:

```java
boolean isWrapper = ClassTypeUtils.isPrimitiveOrWrapper(Integer.class); // true
Integer value = (Integer) ClassTypeUtils.convertPrimitiveOrWrapper(Integer.class, "123");
Boolean flag = (Boolean) ClassTypeUtils.convertPrimitiveOrWrapper(Boolean.class, "true");
```

---

## 六、Jackson 序列化配置

### 6.1 JacksonModuleConfigurer

- Long/long 类型全局序列化为 String，避免前端 JavaScript 精度丢失

### 6.2 ExtendDateFormat

自定义日期格式解析器，支持以下格式:

| 格式 | 示例 |
|------|------|
| 标准日期 | `yyyy-MM-dd HH:mm:ss` |
| 年月 | `yyyy-MM` |
| ISO8601 | `yyyy-MM-dd'T'HH:mm:ss` |
| 13 位时间戳 | `1700000000000` |

---

## 七、全局常量

源码路径: `lambda-cloud-core/src/main/java/com/lambda/cloud/core/Constants.java`

| 常量 | 值 | 说明 |
|------|-----|------|
| `HMAC` | `"hmac"` | HMAC 认证标识 |
| `LOGIN_USER` | `"loginUser"` | 登录用户标识 |
| `LOGIN_TYPE` | `"loginType"` | 登录类型标识 |
| `LOGIN_DEVICE` | `"loginDevice"` | 登录设备标识 |
| `ANONYMOUS_USER` | `AnonymousUser` 实例 | 匿名用户单例 |
| `GSON` | `Gson` 实例 | JSON 序列化实例 |

---

## 八、关键约定

### 8.1 转换器命名约定

运行时按 `源类型全限定名 + "Converter"` 查找转换器 Bean。转换器需实现 `BaseConverter` 并注册为 Spring Bean。

### 8.2 isReverse 方向约定

- `isReverse=false`（默认）: 生成 `BaseConverter<Target, Source>`
- `isReverse=true`: 生成 `BaseConverter<Source, Target>`
- 实际使用时以生成后的 `convertTo` 方法签名为准

### 8.3 分页默认值

- `pageNum` 默认 1，最小 1
- `pageSize` 默认 20，最小 1，最大 1000

### 8.4 异常映射

- Web 层: 统一转 `ErrorModel` JSON 响应
- Feign 层: 转对应状态码的 `Feign*Exception` 异常类

---

## 九、完整使用示例

### DTO 转 Entity

```java
@AutoConverter(target = UserEntity.class, isReverse = true)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;

    @FieldMapping(target = "status", defaultValue = "ACTIVE")
    private String status;
}

UserEntity entity = dto.toEntity();
```

### Entity 转 VO

```java
@AutoConverter(target = UserVO.class)
public class UserVO extends BaseVO<UserEntity> {
    private String username;
    private String status;
}

UserVO vo = BaseVO.fromEntity(UserVO.class, entity);
List<UserVO> voList = BaseVO.fromEntityList(UserVO.class, entityList);
```

### 使用 ConvertFunctions

```java
@AutoConverter(target = UserEntity.class, isReverse = true)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    @FieldMapping(target = "metadata", qualifiedByName = "mapToString")
    private Map<String, Object> metadata;

    @FieldMapping(target = "createTime", qualifiedByName = "stringToLocalDateTime")
    private String createTime;
}
```

### 获取当前用户

```java
LoginUser user = OperatorUtils.getSafeOperator();
AdminUser admin = OperatorUtils.getLoginUser(AdminUser.class);
```

### 生成 HMAC 认证头

```java
String base = HmacGenerator.baseString(appId, timestamp, queryMap, body);
String authorization = HmacGenerator.authorization(appId, secret, timestamp, base);
```

### 断言校验

```java
Assert.notNull(param, "参数不能为空");
Assert.hasText(param.getName(), "名称不能为空");
Assert.isTrue(param.getAge() > 0, "年龄必须大于0");
Assert.notEmpty(param.getRoles(), "角色不能为空");
```

---

## 十、源码关键路径

| 模块 | 包路径 |
|------|--------|
| 核心包 | `com.lambda.cloud.core` |
| 注解 | `com.lambda.cloud.core.annotation` |
| 转换 | `com.lambda.cloud.core.convert` |
| 共享模型 | `com.lambda.cloud.core.shared` |
| 安全主体 | `com.lambda.cloud.core.principal` |
| 工具类 | `com.lambda.cloud.core.utils` |
| 异常 | `com.lambda.cloud.core.exception` |
| Jackson | `com.lambda.cloud.core.jackson` |
| 全局常量 | `com.lambda.cloud.core.Constants` |
