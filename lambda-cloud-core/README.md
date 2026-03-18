# Lambda Cloud Core

`lambda-cloud-core` 是 `lambda_cloud_parent` 的基础公共模块，提供模型基类、对象转换、异常模型、安全上下文与通用工具能力，供上层 starter 和业务模块直接复用。

## 模块定位

- 提供统一的 DTO/VO/DO/分页抽象。
- 提供 MapStruct 场景下的转换注解与运行时解析能力。
- 提供统一异常模型及 Feign 侧状态码异常封装。
- 提供 Sa-Token 登录上下文辅助工具。
- 提供通用工具类（断言、类型转换、签名、日期解析）。

## 包结构

```text
src/main/java/com/lambda/cloud/core
├─ annotation/      # @AutoConverter / @FieldMapping
├─ convert/         # BaseConverter / ConvertFunctions / ConverterResolver
├─ shared/          # BaseDO / BaseDTO / BaseVO / BasePageDTO / PageRequest
├─ principal/       # LoginUser / AnonymousUser
├─ utils/           # Assert / ConvertUtils / OperatorUtils / StpLogicUtils / HmacGenerator
├─ exception/       # 基础异常、Feign异常、错误模型
├─ jackson/         # JacksonModuleConfigurer / ExtendDateFormat
└─ Constants.java   # 全局常量
```

## 核心能力

### 1) 通用模型

- `BaseDO`：审计字段统一（`createUser/createTime/updateUser/updateTime`）。
- `BaseDTO<E>`：提供 `toEntity()` 与查询包装器基础方法。
- `BaseVO<E>`：提供 `fromEntity` / `fromEntityList` 静态转换入口。
- `BasePageDTO<T>`：统一分页参数（默认 `pageNum=1`、`pageSize=20`，最大 `1000`）。
- `PageRequest<T>`：提供 `getPage()`，直接生成 MyBatis-Plus `Page`。

### 2) 对象转换

- `@AutoConverter`：声明转换目标类型和方向。
- `@FieldMapping`：声明字段映射规则，支持表达式、格式化、条件映射等。
- `BaseConverter<S, T>`：统一转换接口与集合转换默认方法。
- `ConvertFunctions`：MapStruct `@Named` 转换函数库。
- `ConverterResolver`：按约定查找 `XxxConverter` Bean，并做缓存。
- `ConvertUtils`：常用单对象/列表转换快捷入口。

### 3) 安全上下文

- `LoginUser`：统一登录用户主体接口。
- `AnonymousUser`：匿名用户默认实现（最小权限）。
- `StpLogicUtils`：多登录类型管理、会话查找、活跃登录逻辑识别。
- `OperatorUtils`：获取当前操作人，支持安全降级。

### 4) 异常模型

- 基础异常：`IllegalAccessException`、`IllegalArgumentException`、`IllegalStateException`、`NotSupportedException`。
- 错误模型：`ErrorCode`、`ErrorModel`、`ArgumentError`。
- Feign 异常：
  - `FeignArgumentNotValidException`（400）
  - `FeignUnauthorizedException`（401）
  - `FeignAccessDeniedException`（403）
  - `FeignInternalServerErrorException`（500）
  - `FeignServiceNotAvailableException`（503）

### 5) 序列化与日期处理

- `JacksonModuleConfigurer`：Long/long 序列化为字符串，避免前端精度丢失。
- `ExtendDateFormat`：支持标准日期、年月、ISO8601、13位时间戳解析。

### 6) 工具能力

- `Assert`：统一断言与参数校验。
- `TypeConverter` / `ClassTypeUtils`：常见类型转换辅助。
- `HmacGenerator`：生成 HMAC 签名串与认证头。
- `Constants`：认证、日期格式、分页提示、websocket 等全局常量。

## 关键约定

### 转换器命名约定

- 运行时按 `源类型全限定名 + "Converter"` 查找转换器。
- 转换器需实现 `BaseConverter` 并注册为 Spring Bean。

### `isReverse` 方向约定

- `isReverse=false`：默认方向。
- `isReverse=true`：反向生成泛型顺序与 `convertTo` 入参/返回值。
- 实际使用时以生成后的 `convertTo` 方法签名为准。

## 使用示例

### DTO 转 Entity

```java
@AutoConverter(target = UserEntity.class)
public class UserCreateDTO extends BaseDTO<UserEntity> {
    private String username;
}

UserEntity entity = dto.toEntity();
```

### Entity 转 VO

```java
UserVO vo = BaseVO.fromEntity(UserVO.class, entity);
```

### 获取当前用户

```java
LoginUser currentUser = OperatorUtils.getSafeOperator();
```

### 生成 HMAC 认证头

```java
String base = HmacGenerator.baseString(appid, timestamp, queryMap, body);
String authorization = HmacGenerator.authorization(appid, secret, timestamp, base);
```

## 资源配置

`src/main/resources` 提供模块默认配置，主要覆盖：

- `application.yaml`：Undertow、Feign、Dubbo、MyBatis-Plus、Sa-Token 等基础项。
- `config/application.properties`：Nacos、Actuator、SpringDoc 等默认参数。

上层业务建议按环境覆盖，不直接改动模块默认值。
